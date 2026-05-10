package com.ruoyi.gserve.datasource;

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
 * ClickHouse 数据源切换 AOP
 * <p>
 * 拦截 {@link ClickHouseDb} 注解，自动切换 ClickHouse 数据源。
 * projectId 优先级：注解 > 请求头 X-Project-Id > 请求头 X-App-Id（通过映射）
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

        // 注解中未指定 projectId，从请求上下文获取
        if (projectId <= 0) {
            projectId = resolveProjectIdFromRequest();
        }

        if (projectId > 0) {
            ClickHouseContextHolder.setProjectId(projectId);
            log.debug("切换 ClickHouse 数据源: projectId={}", projectId);
        }

        try {
            return point.proceed();
        } finally {
            ClickHouseContextHolder.clear();
        }
    }

    /**
     * 从请求头解析 projectId
     * 优先使用 X-Project-Id，其次从 X-App-Id 映射
     */
    private long resolveProjectIdFromRequest() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) {
                return 0;
            }
            HttpServletRequest request = attrs.getRequest();

            // 1. 直接取 X-Project-Id 请求头
            String projectIdStr = request.getHeader("X-Project-Id");
            if (projectIdStr != null && !projectIdStr.isEmpty()) {
                return Long.parseLong(projectIdStr);
            }

            // 2. 从 X-App-Id 映射（Gateway 注入 app_id）
            // TODO: 实现 appId -> projectId 的映射查询
            // String appId = request.getHeader("X-App-Id");
            // if (appId != null) { return mapAppIdToProjectId(appId); }
        } catch (Exception e) {
            log.warn("从请求解析 projectId 失败", e);
        }
        return 0;
    }
}
