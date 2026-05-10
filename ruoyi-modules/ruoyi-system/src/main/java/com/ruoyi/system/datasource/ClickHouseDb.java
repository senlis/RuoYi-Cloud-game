package com.ruoyi.system.datasource;

import java.lang.annotation.*;

/**
 * ClickHouse 数据源注解（查询端）
 * <p>
 * 标注在方法上，AOP 自动根据 projectId 切换到对应的 ClickHouse 数据源。
 * projectId 为空时尝试从请求上下文自动获取。
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
