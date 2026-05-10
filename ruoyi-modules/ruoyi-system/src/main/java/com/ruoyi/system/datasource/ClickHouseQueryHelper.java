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
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * ClickHouse 查询辅助工具
 * <p>
 * 按需获取 ClickHouse 连接。从 game_project.clickhouse_config 读取项目配置，
 * DataSource 缓存复用。
 * <p>
 * 使用方式：直接注入，调用 {@code getConnection(projectId)}
 *
 * @author ruoyi
 */
@Component
public class ClickHouseQueryHelper {

    private static final Logger log = LoggerFactory.getLogger(ClickHouseQueryHelper.class);

    private static final String JDBC_URL_TEMPLATE = "jdbc:clickhouse://%s:%d/%s";
    private static final String REDIS_CONFIG_PREFIX = "game:project:clickhouse:";
    private static final long REDIS_TTL = 300;

    private final Map<Long, DataSource> dsCache = new ConcurrentHashMap<>();

    /**
     * 获取指定项目的 ClickHouse 连接
     */
    public Connection getConnection(Long projectId) throws SQLException {
        DataSource ds = dsCache.computeIfAbsent(projectId, this::createDataSource);
        return ds.getConnection();
    }

    private DataSource createDataSource(Long projectId) {
        String configJson = getConfig(projectId);
        if (configJson == null || configJson.isBlank()) {
            throw new RuntimeException("项目 " + projectId + " 未配置 ClickHouse");
        }

        JSONObject c = JSON.parseObject(configJson);
        String url = String.format(JDBC_URL_TEMPLATE,
                c.getString("host"),
                c.getIntValue("port", 8123),
                c.getString("database"));
        Properties props = new Properties();
        props.setProperty("user", c.getString("username", "default"));
        props.setProperty("password", c.getString("password", ""));
        props.setProperty("connectTimeout", "10000");
        props.setProperty("socketTimeout", "30000");
        props.setProperty("compress", "0");

        try {
            ClickHouseDataSource ds = new ClickHouseDataSource(url, props);
            log.info("创建 ClickHouse 数据源: projectId={}, url={}", projectId, url);
            return ds;
        } catch (SQLException e) {
            throw new RuntimeException("创建 ClickHouse 数据源失败", e);
        }
    }

    /**
     * 获取 ClickHouse 配置（Redis → MySQL）
     */
    private String getConfig(Long projectId) {
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
        GameProjectMapper mapper = SpringUtils.getBean(GameProjectMapper.class);
        if (mapper == null) throw new RuntimeException("GameProjectMapper 不可用");

        GameProject project = mapper.selectGameProjectById(projectId);
        if (project == null || project.getClickhouseConfig() == null
                || project.getClickhouseConfig().isBlank()) {
            return null;
        }

        String config = project.getClickhouseConfig();
        // 回写 Redis
        try {
            RedisService rs = SpringUtils.getBean(RedisService.class);
            if (rs != null) rs.setCacheObject(REDIS_CONFIG_PREFIX + projectId, config, REDIS_TTL, TimeUnit.SECONDS);
        } catch (Exception ignored) {}

        return config;
    }

    /**
     * 移除缓存
     */
    public void evict(Long projectId) {
        dsCache.remove(projectId);
    }
}
