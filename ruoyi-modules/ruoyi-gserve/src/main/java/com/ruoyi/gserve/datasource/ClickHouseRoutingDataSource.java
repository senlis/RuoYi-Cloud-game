package com.ruoyi.gserve.datasource;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.clickhouse.jdbc.ClickHouseDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ClickHouse 动态路由数据源
 * <p>
 * 根据 project_id 动态切换 ClickHouse 数据源。
 * 连接信息从 game_project.clickhouse_config JSON 字段读取，格式：
 * <pre>
 * {
 *   "host": "192.168.1.100",
 *   "port": 8123,
 *   "database": "game_analytics_mhxx",
 *   "username": "default",
 *   "password": "",
 *   "minPoolSize": 2,
 *   "maxPoolSize": 8
 * }
 * </pre>
 * <p>
 * 使用方式：通过 {@link com.ruoyi.gserve.datasource.ClickHouseDb} 注解 + AOP 切换
 * 或直接调用 {@code ClickHouseContextHolder.setProjectId(projectId)}
 *
 * @author ruoyi
 */
public class ClickHouseRoutingDataSource extends AbstractRoutingDataSource {

    private static final Logger log = LoggerFactory.getLogger(ClickHouseRoutingDataSource.class);

    /** 项目 ClickHouse 数据源缓存 */
    private static final Map<Long, DataSource> DATA_SOURCE_CACHE = new ConcurrentHashMap<>();

    /** JDBC URL 模板 */
    private static final String JDBC_URL_TEMPLATE = "jdbc:clickhouse://%s:%d/%s";

    @Override
    protected Object determineCurrentLookupKey() {
        Long projectId = ClickHouseContextHolder.getProjectId();
        return projectId != null ? projectId : "default";
    }

    @Override
    protected DataSource determineTargetDataSource() {
        Object lookupKey = determineCurrentLookupKey();

        if (lookupKey instanceof Long projectId) {
            // 从缓存获取或创建
            return DATA_SOURCE_CACHE.computeIfAbsent(projectId, this::createDataSource);
        }

        // 默认情况（无 project_id），返回空兜底
        log.warn("未指定 ClickHouse projectId，使用默认空数据源");
        return super.determineTargetDataSource();
    }

    /**
     * 根据 projectId 创建 ClickHouse 数据源
     * <p>
     * 连接信息从 game_project.clickhouse_config 字段读取，
     * 通过 feign 调用或直接查库获取
     *
     * @param projectId 项目 ID
     * @return ClickHouse DataSource
     */
    private DataSource createDataSource(Long projectId) {
        try {
            // 获取项目 ClickHouse 配置 JSON
            String configJson = getClickHouseConfig(projectId);
            if (configJson == null || configJson.isBlank()) {
                throw new RuntimeException("项目 " + projectId + " 未配置 ClickHouse");
            }

            JSONObject config = JSON.parseObject(configJson);
            String host = config.getString("host");
            int port = config.getIntValue("port", 8123);
            String database = config.getString("database");
            String username = config.getString("username");
            String password = config.getString("password");

            String url = String.format(JDBC_URL_TEMPLATE, host, port, database);
            Properties props = new Properties();
            props.setProperty("user", username != null ? username : "default");
            props.setProperty("password", password != null ? password : "");
            props.setProperty("connectTimeout", "10000");
            props.setProperty("socketTimeout", "30000");
            props.setProperty("compress", "0");

            ClickHouseDataSource ds = new ClickHouseDataSource(url, props);
            log.info("创建 ClickHouse 数据源成功: projectId={}, url={}", projectId, url);
            return ds;
        } catch (SQLException e) {
            log.error("创建 ClickHouse 数据源失败: projectId={}", projectId, e);
            throw new RuntimeException("创建 ClickHouse 数据源失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取项目 ClickHouse 配置
     * <p>
     * 优先从 Redis 缓存读取，缓存未命中则通过 MyBatis-Plus 查 MySQL
     *
     * @param projectId 项目 ID
     * @return clickhouse_config JSON 字符串
     */
    private String getClickHouseConfig(Long projectId) {
        // 1. 先尝试从 Redis 缓存读取
        try {
            com.ruoyi.common.redis.service.RedisService redisService =
                    com.ruoyi.common.core.utils.SpringUtils.getBean(com.ruoyi.common.redis.service.RedisService.class);
            if (redisService != null) {
                String cached = redisService.getCacheObject("game:project:clickhouse:" + projectId);
                if (cached != null && !cached.isBlank()) {
                    return cached;
                }
            }
        } catch (Exception e) {
            log.debug("Redis 缓存读取失败，回退到 MySQL", e);
        }

        // 2. Redis 未命中，通过 MyBatis 从 MySQL game_project 表读取
        try {
            com.ruoyi.gserve.mapper.GameProjectChConfigMapper mapper =
                    com.ruoyi.common.core.utils.SpringUtils.getBean(com.ruoyi.gserve.mapper.GameProjectChConfigMapper.class);
            if (mapper == null) {
                throw new RuntimeException("GameProjectChConfigMapper 未初始化");
            }

            com.ruoyi.gserve.domain.GameProjectChConfig project = mapper.selectById(projectId);
            if (project != null) {
                String config = project.getClickhouseConfig();
                if (config != null && !config.isBlank()) {
                    // 回写 Redis 缓存（300 秒 TTL）
                    try {
                        com.ruoyi.common.redis.service.RedisService redisService =
                                com.ruoyi.common.core.utils.SpringUtils.getBean(com.ruoyi.common.redis.service.RedisService.class);
                        if (redisService != null) {
                            redisService.setCacheObject("game:project:clickhouse:" + projectId,
                                    config, 300L, java.util.concurrent.TimeUnit.SECONDS);
                        }
                    } catch (Exception e) {
                        log.debug("Redis 回写缓存失败", e);
                    }
                    return config;
                }
            }
        } catch (Exception e) {
            log.error("从 MySQL 查询 clickhouse_config 失败: projectId={}", projectId, e);
        }

        throw new RuntimeException("项目 " + projectId + " 的 ClickHouse 配置未找到（请检查项目管理页面是否已配置）");
    }

    /**
     * 预创建指定项目的数据源（启动时调用）
     */
    public static void preloadDataSource(Long projectId, String configJson) {
        try {
            JSONObject config = JSON.parseObject(configJson);
            String host = config.getString("host");
            int port = config.getIntValue("port", 8123);
            String database = config.getString("database");
            String username = config.getString("username");
            String password = config.getString("password");

            String url = String.format(JDBC_URL_TEMPLATE, host, port, database);
            Properties props = new Properties();
            props.setProperty("user", username != null ? username : "default");
            props.setProperty("password", password != null ? password : "");
            props.setProperty("connectTimeout", "10000");
            props.setProperty("socketTimeout", "30000");
            props.setProperty("compress", "0");

            ClickHouseDataSource ds = new ClickHouseDataSource(url, props);
            DATA_SOURCE_CACHE.put(projectId, ds);
            log.info("预加载 ClickHouse 数据源: projectId={}, url={}", projectId, url);
        } catch (Exception e) {
            log.error("预加载 ClickHouse 数据源失败: projectId={}", projectId, e);
        }
    }

    /**
     * 移除并关闭指定项目的数据源（配置变更时调用）
     */
    public static void removeDataSource(Long projectId) {
        DataSource ds = DATA_SOURCE_CACHE.remove(projectId);
        if (ds != null) {
            log.info("移除 ClickHouse 数据源: projectId={}", projectId);
        }
    }

    /**
     * 清空所有数据源缓存（服务重启时调用）
     */
    public static void clearAll() {
        DATA_SOURCE_CACHE.clear();
        log.info("清空所有 ClickHouse 数据源缓存");
    }
}
