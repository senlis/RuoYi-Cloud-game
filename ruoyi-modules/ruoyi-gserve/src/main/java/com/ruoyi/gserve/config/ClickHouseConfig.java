package com.ruoyi.gserve.config;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.clickhouse.jdbc.ClickHouseDataSource;
import com.ruoyi.common.redis.service.RedisService;
import com.ruoyi.common.core.utils.SpringUtils;
import com.ruoyi.gserve.mapper.GameProjectChConfigMapper;
import com.ruoyi.gserve.domain.GameProjectChConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * ClickHouse 连接管理器
 * <p>
 * 根据 project_id 从 game_project.clickhouse_config 读取配置，
 * 创建并缓存 ClickHouse DataSource（Redis 缓存配置，按 projectId 缓存 DataSource）。
 *
 * @author ruoyi
 */
@Configuration
public class ClickHouseConfig {

    private static final Logger log = LoggerFactory.getLogger(ClickHouseConfig.class);

    private static final String JDBC_URL_TEMPLATE = "jdbc:clickhouse://%s:%d/%s";
    private static final String REDIS_CONFIG_PREFIX = "game:project:clickhouse:";
    private static final long REDIS_TTL = 300;

    private final Map<Long, DataSource> dataSourceCache = new ConcurrentHashMap<>();

    /**
     * 根据 project_id 获取 ClickHouse 连接（自动从 Redis/MySQL 读取配置）
     */
    public Connection getConnection(Long projectId) throws SQLException {
        DataSource ds = dataSourceCache.computeIfAbsent(projectId, pid -> {
            String configJson = resolveConfig(pid);
            if (configJson == null) {
                log.error("项目 {} 未配置 ClickHouse", pid);
                return null;
            }
            try {
                return createDataSource(configJson);
            } catch (Exception e) {
                log.error("创建 ClickHouse 数据源失败: projectId={}", pid, e);
                return null;
            }
        });

        if (ds == null) {
            throw new SQLException("无法创建 ClickHouse 数据源: projectId=" + projectId);
        }
        return ds.getConnection();
    }

    /**
     * 测试连接
     */
    public boolean testConnection(Long projectId) {
        try (Connection conn = getConnection(projectId)) {
            return conn.isValid(3);
        } catch (Exception e) {
            log.error("ClickHouse 连接测试失败: projectId={}", projectId, e);
            return false;
        }
    }

    /**
     * 解析项目的 ClickHouse 配置（Redis → MySQL）
     */
    private String resolveConfig(Long projectId) {
        // 1. Redis
        try {
            RedisService rs = SpringUtils.getBean(RedisService.class);
            if (rs != null) {
                String cached = rs.getCacheObject(REDIS_CONFIG_PREFIX + projectId);
                if (cached != null && !cached.isBlank()) return cached;
            }
        } catch (Exception e) {
            log.debug("Redis 未命中", e);
        }

        // 2. MySQL
        try {
            GameProjectChConfigMapper mapper = SpringUtils.getBean(GameProjectChConfigMapper.class);
            if (mapper == null) return null;
            GameProjectChConfig project = mapper.selectById(projectId);
            if (project == null || project.getClickhouseConfig() == null
                    || project.getClickhouseConfig().isBlank()) {
                return null;
            }
            String config = project.getClickhouseConfig();
            // 回写 Redis
            try {
                RedisService rs = SpringUtils.getBean(RedisService.class);
                if (rs != null)
                    rs.setCacheObject(REDIS_CONFIG_PREFIX + projectId, config, REDIS_TTL, TimeUnit.SECONDS);
            } catch (Exception ignored) {}
            return config;
        } catch (Exception e) {
            log.error("读取项目 ClickHouse 配置失败: projectId={}", projectId, e);
            return null;
        }
    }

    private DataSource createDataSource(String configJson) {
        JSONObject config = JSON.parseObject(configJson);
        String host = config.getString("host");
        int port = config.getIntValue("port", 8123);
        String database = config.getString("database");
        String username = config.getString("username", "default");
        String password = config.getString("password", "");

        String url = String.format(JDBC_URL_TEMPLATE, host, port, database);
        Properties props = new Properties();
        props.setProperty("user", username);
        props.setProperty("password", password);
        props.setProperty("connectTimeout", "10000");
        props.setProperty("socketTimeout", "30000");
        props.setProperty("compress", "0");

        try {
            return new ClickHouseDataSource(url, props);
        } catch (SQLException e) {
            throw new RuntimeException("创建 ClickHouse DataSource 失败: " + url, e);
        }
    }

    /**
     * 移除缓存的数据源（配置变更时调用）
     */
    public void removeDataSource(Long projectId) {
        DataSource ds = dataSourceCache.remove(projectId);
        if (ds != null) log.info("移除 ClickHouse 数据源: projectId={}", projectId);
    }
}
