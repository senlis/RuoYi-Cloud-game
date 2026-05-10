package com.ruoyi.gserve.datasource;

/**
 * ClickHouse 上下文持有者
 * <p>
 * 通过 ThreadLocal 存储当前线程的 projectId，
 * 供 {@link ClickHouseRoutingDataSource} 在运行时动态切换数据源。
 *
 * @author ruoyi
 */
public class ClickHouseContextHolder {

    private static final ThreadLocal<Long> PROJECT_ID_HOLDER = new ThreadLocal<>();

    /**
     * 设置当前线程的项目 ID
     */
    public static void setProjectId(Long projectId) {
        PROJECT_ID_HOLDER.set(projectId);
    }

    /**
     * 获取当前线程的项目 ID
     */
    public static Long getProjectId() {
        return PROJECT_ID_HOLDER.get();
    }

    /**
     * 清除当前线程的项目 ID（务必在 finally 中调用）
     */
    public static void clear() {
        PROJECT_ID_HOLDER.remove();
    }
}
