package com.ruoyi.gserve.datasource;

import java.lang.annotation.*;

/**
 * ClickHouse 数据源注解
 * <p>
 * 标注在方法上，AOP 自动根据 projectId 切换到对应的 ClickHouse 数据源。
 * projectId 为空时尝试从请求上下文自动获取。
 * <p>
 * 使用示例：
 * <pre>
 * // 从请求上下文获取 projectId
 * &#64;ClickHouseDb
 * public void writeEvents(List<EventEntry> events) { ... }
 *
 * // 指定 projectId
 * &#64;ClickHouseDb(projectId = 1)
 * public void queryByProject(List<EventEntry> events) { ... }
 * </pre>
 *
 * @author ruoyi
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ClickHouseDb {

    /** 项目 ID，为 0 时从请求上下文自动获取 */
    long projectId() default 0;
}
