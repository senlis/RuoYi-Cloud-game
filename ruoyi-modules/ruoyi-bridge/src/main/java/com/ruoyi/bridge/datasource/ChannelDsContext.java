package com.ruoyi.bridge.datasource;

/**
 * 渠道数据源上下文持有者
 * <p>
 * 使用 ThreadLocal 存储当前线程应使用的渠道数据源标识（channelKey）。
 * 由 {@link ChannelDsAspect} 在方法执行前设置，在方法执行后清除。
 *
 * @author ruoyi
 */
public class ChannelDsContext {

    /**
     * ThreadLocal 持有当前线程的数据源标识
     */
    private static final ThreadLocal<String> HOLDER = new ThreadLocal<>();

    /**
     * 设置当前线程的数据源标识
     *
     * @param channelKey 渠道KEY
     */
    public static void set(String channelKey) {
        HOLDER.set(channelKey);
    }

    /**
     * 获取当前线程的数据源标识
     *
     * @return 渠道KEY，如果没有设置则返回 null
     */
    public static String get() {
        return HOLDER.get();
    }

    /**
     * 清除当前线程的数据源标识
     * <p>
     * 在方法执行完毕后必须调用，防止内存泄漏或数据源错乱。
     */
    public static void clear() {
        HOLDER.remove();
    }
}
