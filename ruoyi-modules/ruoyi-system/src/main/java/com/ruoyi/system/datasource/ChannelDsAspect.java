package com.ruoyi.system.datasource;

import com.ruoyi.system.domain.BrPlatformConfig;
import com.ruoyi.system.helper.GameRoutingDataSource;
import com.ruoyi.system.mapper.BrPlatformConfigMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 渠道数据源切面
 * <p>
 * 拦截所有标注了 {@link ChannelDs} 注解的方法，解析 SpEL 表达式获取 channelKey，
 * 利用 {@link GameRoutingDataSource} 动态注册并切换到对应渠道的独立数据库。
 * 方法执行完毕后自动重置回主数据源。
 *
 * @author ruoyi
 */
@Aspect
@Component
public class ChannelDsAspect {

    private static final Logger log = LoggerFactory.getLogger(ChannelDsAspect.class);

    private static final ParameterNameDiscoverer PARAMETER_NAME_DISCOVERER = new DefaultParameterNameDiscoverer();
    private static final ExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();

    @Autowired
    private GameRoutingDataSource gameRoutingDataSource;

    @Autowired
    private BrPlatformConfigMapper platformConfigMapper;

    @Around("@annotation(channelDs)")
    public Object around(ProceedingJoinPoint joinPoint, ChannelDs channelDs) throws Throwable {
        // 解析 channelKey：优先 SpEL，失败则取第一个 String 参数
        String channelKey = resolveChannelKey(joinPoint, channelDs);

        if (channelKey == null || channelKey.isEmpty()) {
            log.warn("@ChannelDs 未解析到 channelKey，使用默认数据源");
            return joinPoint.proceed();
        }

        // 保存并清除 PageHelper 分页上下文，避免被 config 查询消费掉
        // startPage() 在 controller 层调用，但这里会先执行一次 MyBatis 查询（selectByChannelKey），
        // 如果不清除，PageHelper 会将分页信息误用于 config 查询，导致实际数据查询无分页
        com.github.pagehelper.Page page = com.github.pagehelper.page.PageMethod.getLocalPage();
        com.github.pagehelper.page.PageMethod.clearPage();

        BrPlatformConfig config = platformConfigMapper.selectByChannelKey(channelKey);

        // 恢复分页上下文，供后续的实际数据查询使用
        if (page != null) {
            com.github.pagehelper.page.PageMethod.setLocalPage(page);
        }
        if (config == null) {
            log.warn("渠道 {} 未配置平台信息，使用默认数据源", channelKey);
            return joinPoint.proceed();
        }
        if ("1".equals(config.getStatus())) {
            throw new RuntimeException("渠道 " + channelKey + " 已停用，无法操作");
        }

        try {
            // 注册渠道数据源到 GameRoutingDataSource
            String dsName = "channel_" + channelKey;
            if (config.getDbHost() != null && !config.getDbHost().isEmpty()) {
                gameRoutingDataSource.getOrCreateChannelDb(channelKey, config);
            }
            // 切换到渠道数据源
            GameRoutingDataSource.push(dsName);
            log.info("切换数据源 -> {} (host: {})", dsName, config.getDbHost());
            return joinPoint.proceed();
        } finally {
            GameRoutingDataSource.poll();
            log.debug("恢复数据源 -> master");
        }
    }

    /**
     * 解析 channelKey
     * <p>
     * 优先通过 SpEL 表达式获取；若失败（项目未启用 -parameters 编译参数时），
     * 自动取方法第一个 String 类型参数作为 channelKey。
     */
    private String resolveChannelKey(ProceedingJoinPoint jp, ChannelDs channelDs) {
        String spel = channelDs.value();
        if (spel == null || spel.isEmpty()) {
            // 无 SpEL 表达式时，取第一个 Number 参数作为 channelId
            for (Object arg : jp.getArgs()) {
                if (arg instanceof Number) {
                    Long channelId = ((Number) arg).longValue();
                    BrPlatformConfig cfg = platformConfigMapper.selectById(channelId);
                    return cfg != null ? cfg.getChannelKey() : null;
                }
            }
            return null;
        }

        // 方式一：通过 DefaultParameterNameDiscoverer 解析 SpEL
        // （支持 -g 编译的调试信息，在没有 -parameters 时也能读取参数名）
        Method method = ((MethodSignature) jp.getSignature()).getMethod();
        Object[] args = jp.getArgs();
        String[] paramNames = PARAMETER_NAME_DISCOVERER.getParameterNames(method);

        if (paramNames != null) {
            try {
                StandardEvaluationContext ctx = new StandardEvaluationContext();
                for (int i = 0; i < paramNames.length && i < args.length; i++) {
                    ctx.setVariable(paramNames[i], args[i]);
                }
                Object value = EXPRESSION_PARSER.parseExpression(spel).getValue(ctx);
                if (value != null) {
                    return String.valueOf(value);
                }
            } catch (Exception e) {
                log.debug("SpEL 解析失败: {}，尝试备用方式", spel);
            }
        }

        // 方式二：取第一个 String 类型参数作为 channelKey（兼容无参数名场景）
        for (Object arg : args) {
            if (arg instanceof String) {
                return (String) arg;
            }
        }

        return null;
    }
}
