package com.ruoyi.system.datasource;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.clickhouse.jdbc.ClickHouseDataSource;
import com.ruoyi.common.core.utils.SpringUtils;
import com.ruoyi.common.redis.service.RedisService;
import com.ruoyi.system.domain.GameProject;
import com.ruoyi.system.mapper.GameProjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * ClickHouse 动态路由数据源（查询端）
 * <p>
 * 用于 system 模块的统计查询，根据 project_id 动态切换。
 * 连接信息从 game_project.clickhouse_config JSON 字段读取，
 * 由项目管理页面配置。
 * <p>
 * 缓存策略：
 * - 数据源实例：ConcurrentHashMap 缓存，随 projectId 生命周期
 * - clickhouse_config：Redis 缓存（TTL 300 秒），减少频繁读 MySQL
 *
 * @author ruoyi
 */
public class ClickHouseRoutingDataSource extends AbstractRoutingDataSource {

    private static final Logger log = LoggerFactory.getLogger(ClickHouseRoutingDataSource.class);

    /** 数据源缓存 */
    private static final Map<Long, DataSource> DATA_SOURCE_CACHE = new ConcurrentHashMap<>();

    /** JDBC URL 模板 */
    private static final String JDBC_URL_TEMPLATE = "jdbc:clickhouse://%s:%d/%s";

    /** Redis 缓存前缀 */
    private static final String REDIS_CONFIG_PREFIX = "game:project:clickhouse:";

    @Override
    protected Object determineCurrentLookupKey() {
        Long projectId = ClickHouseContextHolder.getProjectId();
        return projectId != null ? projectId : "default";
    }

    @Override
    protected DataSource determineTargetDataSource() {
        Object lookupKey = determineCurrentLookupKey();
        if (lookupKey instanceof Long projectId) {
            return DATA_SOURCE_CACHE.computeIfAbsent(projectId, this::createDataSource);
        }
        return super.determineTargetDataSource();
    }

    /**
     * 根据 projectId 创建 ClickHouse 数据源
     */
    private DataSource createDataSource(Long projectId) {
        try {
            String configJson = getClickHouseConfig(projectId);
            if (configJson == null || configJson.isBlank()) {
                throw new RuntimeException("项目 " + projectId + " 未配置 ClickHouse");
            }

            JSONObject config = JSON.parseObject(configJson);
            String host = config.getString("host");
            int port = config.getIntValue("port", 8123);
            String database = config.getString("database");
            String username = config.getString("username");
            String password = config.getString("password", "");

            String url = String.format(JDBC_URL_TEMPLATE, host, port, database);
            Properties props = new Properties();
            props.setProperty("user", username != null ? username : "default");
            props.setProperty("password", password);
            props.setProperty("connectTimeout", "10000");
            props.setProperty("socketTimeout", "30000");

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
     * 优先从 Redis 缓存读取，缓存未命中则查 MySQL
     */
    private String getClickHouseConfig(Long projectId) {
        try {
            // 1. 尝试从 Redis 读取
            RedisService redisService = SpringUtils.getBean(RedisService.class);
            if (redisService != null) {
                String cached = redisService.getCacheObject(REDIS_CONFIG_PREFIX + projectId);
                if (cached != null) {
                    return cached;
                }
            }
        } catch (Exception e) {
            log.warn("Redis 读取 ClickHouse 配置失败", e);
        }

        // 2. 从 MySQL 读取 game_project.clickhouse_config
        try {
            GameProjectMapper projectMapper = SpringUtils.getBean(GameProjectMapper.class);
            GameProject project = projectMapper.selectGameProjectById(projectId);
            if (project != null) {
                String config = project.getClickhouseConfig();
                if (config != null && !config.isBlank()) {
                    // 写入 Redis 缓存（300 秒 TTL）
                    try {
                        RedisService redisService = SpringUtils.getBean(RedisService.class);
                        redisService.setCacheObject(REDIS_CONFIG_PREFIX + projectId,
                                config, 300L, TimeUnit.SECONDS);
                    } catch (Exception e) {
                        log.warn("Redis 缓存 ClickHouse 配置失败", e);
                    }
                    return config;
                }
            }
        } catch (Exception e) {
            log.error("查询项目 ClickHouse 配置失败: projectId={}", projectId, e);
        }

        return null;
    }

    /**
     * 预创建数据源
     */
    public static void preloadDataSource(Long projectId, String configJson) {
        try {
            JSONObject config = JSON.parseObject(configJson);
            String host = config.getString("host");
            int port = config.getIntValue("port", 8123);
            String database = config.getString("database");
            String username = config.getString("username");
            String password = config.getString("password", "");

            String url = String.format(JDBC_URL_TEMPLATE, host, port, database);
            Properties props = new Properties();
            props.setProperty("user", username != null ? username : "default");
            props.setProperty("password", password);
            props.setProperty("connectTimeout", "10000");
            props.setProperty("socketTimeout", "30000");

            ClickHouseDataSource ds = new ClickHouseDataSource(url, props);
            DATA_SOURCE_CACHE.put(projectId, ds);
            log.info("预加载 ClickHouse 数据源: projectId={}", projectId);
        } catch (Exception e) {
            log.error("预加载 ClickHouse 数据源失败: projectId={}", projectId, e);
        }
    }

    /**
     * 移除数据源（配置变更时）
     */
    public static void removeDataSource(Long projectId) {
        DATA_SOURCE_CACHE.remove(projectId);
        log.info("移除 ClickHouse 数据源: projectId={}", projectId);
    }

    /**
     * 清空缓存
     */
    public static void clearAll() {
        DATA_SOURCE_CACHE.clear();
        log.info("清空所有 ClickHouse 数据源缓存");
    }
}
