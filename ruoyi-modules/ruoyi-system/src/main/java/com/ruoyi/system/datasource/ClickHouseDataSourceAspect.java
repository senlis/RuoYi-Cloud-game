package com.ruoyi.system.datasource;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * ClickHouse 数据源切换 AOP（查询端）
 * <p>
 * 拦截 @ClickHouseDb 注解，自动切换 ClickHouse 数据源。
 * projectId 优先级：注解 > 请求参数 > 请求头 X-Project-Id
 *
 * @author ruoyi
 */
@Aspect
@Component
public class ClickHouseDataSourceAspect {

    private static final Logger log = LoggerFactory.getLogger(ClickHouseDataSourceAspect.class);

    @Around("@annotation(clickHouseDb)")
    public Object around(ProceedingJoinPoint point, ClickHouseDb clickHouseDb) throws Throwable {
        long projectId = clickHouseDb.projectId();

        if (projectId <= 0) {
            projectId = resolveProjectId(point);
        }

        if (projectId > 0) {
            ClickHouseContextHolder.setProjectId(projectId);
        }

        try {
            return point.proceed();
        } finally {
            ClickHouseContextHolder.clear();
        }
    }

    /**
     * 从请求参数或请求头解析 projectId
     */
    private long resolveProjectId(ProceedingJoinPoint point) {
        // 1. 从方法参数找 projectId
        Object[] args = point.getArgs();
        for (Object arg : args) {
            if (arg instanceof Long && (Long) arg > 0) {
                // 第一个 Long 类型参数作为 projectId 候选
                return (Long) arg;
            }
        }

        // 2. 从请求头获取
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                String pid = attrs.getRequest().getParameter("projectId");
                if (pid != null) return Long.parseLong(pid);

                pid = attrs.getRequest().getHeader("X-Project-Id");
                if (pid != null) return Long.parseLong(pid);
            }
        } catch (Exception e) {
            log.warn("解析 projectId 失败", e);
        }

        return 0;
    }
}
