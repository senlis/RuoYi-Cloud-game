package com.ruoyi.system.datasource;

/**
 * ClickHouse 上下文持有者（查询端）
 * <p>
 * 通过 ThreadLocal 存储当前线程的 projectId，
 * 供 ClickHouse 动态数据源路由使用。
 * <p>
 * 与 gserve 模块的 ClickHouseContextHolder 功能一致，
 * 此处为 system 模块独立副本，避免跨模块依赖。
 *
 * @author ruoyi
 */
public class ClickHouseContextHolder {

    private static final ThreadLocal<Long> PROJECT_ID_HOLDER = new ThreadLocal<>();

    public static void setProjectId(Long projectId) {
        PROJECT_ID_HOLDER.set(projectId);
    }

    public static Long getProjectId() {
        return PROJECT_ID_HOLDER.get();
    }

    public static void clear() {
        PROJECT_ID_HOLDER.remove();
    }
}
