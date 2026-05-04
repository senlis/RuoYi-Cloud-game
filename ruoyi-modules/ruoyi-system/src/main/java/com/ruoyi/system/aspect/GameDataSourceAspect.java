package com.ruoyi.system.aspect;

import com.ruoyi.system.annotation.GameDb;
import com.ruoyi.system.annotation.LogDb;
import com.ruoyi.system.service.IGameDataSourceService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 游戏数据源切换切面
 * 拦截 @GameDb 和 @LogDb 注解，动态切换数据源
 *
 * @author ruoyi
 */
@Aspect
@Component
public class GameDataSourceAspect
{
    private static final ParameterNameDiscoverer PARAMETER_NAME_DISCOVERER = new DefaultParameterNameDiscoverer();
    private static final ExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();

    @Autowired
    private IGameDataSourceService gameDataSourceService;

    @Around("@annotation(gameDb)")
    public Object aroundGameDb(ProceedingJoinPoint point, GameDb gameDb) throws Throwable
    {
        Long regionId = resolveLong(gameDb.regionId(), point);
        Integer serverId = resolveInteger(gameDb.serverId(), point);
        return switchAndProceed(point, regionId, serverId, true);
    }

    @Around("@annotation(logDb)")
    public Object aroundLogDb(ProceedingJoinPoint point, LogDb logDb) throws Throwable
    {
        Long regionId = resolveLong(logDb.regionId(), point);
        Integer serverId = resolveInteger(logDb.serverId(), point);
        return switchAndProceed(point, regionId, serverId, false);
    }

    /**
     * 切换数据源并执行目标方法
     */
    private Object switchAndProceed(ProceedingJoinPoint point, Long regionId, Integer serverId, boolean isGameDb) throws Throwable
    {
        boolean switched = false;
        try
        {
            if (isGameDb)
            {
                gameDataSourceService.switchToGameDb(regionId, serverId);
            }
            else
            {
                gameDataSourceService.switchToLogDb(regionId, serverId);
            }
            switched = true;
            return point.proceed();
        }
        finally
        {
            // 只有成功切换后才恢复主库，避免切换失败时错误弹栈
            if (switched)
            {
                com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder.poll();
            }
        }
    }

    /**
     * 解析SpEL表达式为Long
     */
    private Long resolveLong(String spel, ProceedingJoinPoint point)
    {
        Object val = resolve(spel, point);
        if (val == null)
        {
            return null;
        }
        if (val instanceof Number)
        {
            return ((Number) val).longValue();
        }
        return Long.parseLong(val.toString());
    }

    /**
     * 解析SpEL表达式为Integer
     */
    private Integer resolveInteger(String spel, ProceedingJoinPoint point)
    {
        Object val = resolve(spel, point);
        if (val == null)
        {
            return null;
        }
        if (val instanceof Number)
        {
            return ((Number) val).intValue();
        }
        return Integer.parseInt(val.toString());
    }

    /**
     * 解析SpEL表达式
     */
    private Object resolve(String spel, ProceedingJoinPoint point)
    {
        Method method = ((MethodSignature) point.getSignature()).getMethod();
        Object[] args = point.getArgs();
        String[] paramNames = PARAMETER_NAME_DISCOVERER.getParameterNames(method);

        StandardEvaluationContext context = new MethodBasedEvaluationContext(point.getTarget(), method, args, PARAMETER_NAME_DISCOVERER);

        if (spel != null && !spel.isEmpty())
        {
            return EXPRESSION_PARSER.parseExpression(spel).getValue(context);
        }
        return null;
    }
}
