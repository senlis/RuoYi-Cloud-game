package com.ruoyi.system.annotation;

import java.lang.annotation.*;

/**
 * 游戏日志数据库数据源注解
 * 标注在方法上，将数据源切换到指定服务器的游戏日志数据库
 * <p>
 * 使用示例：
 * {@code @LogDb(regionId = "#regionId", serverId = "#serverId")}
 * public void queryLogData(Long regionId, Integer serverId) { ... }
 *
 * @author ruoyi
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogDb
{
    /** 分区ID，支持 SpEL 表达式 */
    String regionId();

    /** 服务器ID，支持 SpEL 表达式 */
    String serverId();
}
