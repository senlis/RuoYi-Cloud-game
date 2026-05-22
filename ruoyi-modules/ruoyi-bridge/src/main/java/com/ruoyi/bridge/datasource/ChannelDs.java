package com.ruoyi.bridge.datasource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 渠道数据源注解
 * <p>
 * 标注在方法上，用于指定该方法使用哪个渠道的独立数据库。
 * value 为 SpEL 表达式，用于从方法参数中获取 channelKey 的值。
 * <p>
 * 示例用法：
 * <pre>
 * &#64;ChannelDs("#channelKey")
 * public void processChannelData(String channelKey) { ... }
 *
 * &#64;ChannelDs("#dto.channelKey")
 * public void processDto(SomeDTO dto) { ... }
 * </pre>
 *
 * @author ruoyi
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ChannelDs {

    /**
     * SpEL 表达式，用于解析 channelKey 参数值。
     * 为空时自动取第一个 int/Integer 参数作为 channelId 去查找平台配置。
     *
     * @return SpEL 表达式
     */
    String value() default "";
}
