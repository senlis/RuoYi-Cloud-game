package com.ruoyi.bridge.datasource;

import com.ruoyi.bridge.domain.BrPlatformConfig;
import com.ruoyi.bridge.service.IBrPlatformConfigService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

/**
 * 渠道数据源切面
 * <p>
 * 拦截所有标注了 {@link ChannelDs} 注解的方法，解析 SpEL 表达式获取 channelKey，
 * 自动切换数据源到对应渠道的独立数据库。方法执行完毕后自动重置回主数据源。
 * <p>
 * 处理流程：
 * <ol>
 *   <li>解析 {@link ChannelDs#value()} 中的 SpEL 表达式，从方法参数中获取 channelKey</li>
 *   <li>根据 channelKey 查询平台配置</li>
 *   <li>若渠道已停用，抛出异常拒绝执行</li>
 *   <li>若渠道配置了独立数据库且尚未注册，自动创建并注册数据源</li>
 *   <li>切换数据源上下文，执行目标方法</li>
 *   <li>方法结束后，清除上下文，回退到主数据源</li>
 * </ol>
 *
 * @author ruoyi
 */
@Slf4j
@Aspect
@Component
public class ChannelDsAspect {

    /** SpEL 表达式解析器 */
    private final SpelExpressionParser parser = new SpelExpressionParser();

    @Autowired
    private IBrPlatformConfigService platformConfigService;

    @Autowired
    private ChannelRoutingDataSource routingDataSource;

    /**
     * 环绕通知：拦截 @ChannelDs 注解的方法
     *
     * @param joinPoint 连接点
     * @return 方法执行结果
     * @throws Throwable 执行异常
     */
    @Around("@annotation(channelDs)")
    public Object around(ProceedingJoinPoint joinPoint, ChannelDs channelDs) throws Throwable {
        // 1. 解析渠道标识 — 优先 SpEL，空则取第一个 int 参数
        String channelKey = null;
        BrPlatformConfig config = null;
        String spel = channelDs.value();

        if (spel != null && !spel.isEmpty()) {
            channelKey = parseSpel(joinPoint, spel, String.class);
            if (channelKey != null) {
                config = platformConfigService.selectByChannelKey(channelKey);
            }
        } else {
            // 从方法参数中取第一个 int/Integer 作为 channelId
            Object first = joinPoint.getArgs().length > 0 ? joinPoint.getArgs()[0] : null;
            if (first instanceof Number) {
                Long channelId = ((Number) first).longValue();
                config = platformConfigService.selectById(channelId);
                if (config != null) {
                    channelKey = config.getChannelKey();
                }
            }
        }

        if (channelKey == null || config == null) {
            log.warn("ChannelDs 未解析到有效渠道配置，直接执行方法");
            return joinPoint.proceed();
        }

        // 2. 检查平台状态
        if ("1".equals(config.getStatus())) {
            throw new RuntimeException("渠道 " + channelKey + " 已停用，无法操作");
        }

        // 3. 切换数据源
        try {
            if (hasDbConfig(config)) {
                routingDataSource.register(channelKey, config);
            }
            routingDataSource.push(channelKey);
            return joinPoint.proceed();
        } finally {
            routingDataSource.poll();
        }
    }

    /** 解析 SpEL */
    private <T> T parseSpel(ProceedingJoinPoint jp, String expr, Class<T> clazz) {
        MethodSignature sig = (MethodSignature) jp.getSignature();
        String[] names = sig.getParameterNames();
        Object[] args = jp.getArgs();
        StandardEvaluationContext ctx = new StandardEvaluationContext();
        for (int i = 0; i < names.length; i++) ctx.setVariable(names[i], args[i]);
        return parser.parseExpression(expr).getValue(ctx, clazz);
    }

    /**
     * 判断平台配置是否包含独立数据库配置
     *
     * @param config 平台配置
     * @return true 如果配置了独立数据库
     */
    private boolean hasDbConfig(BrPlatformConfig config) {
        return config.getDbHost() != null && !config.getDbHost().isEmpty();
    }
}
