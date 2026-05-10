package com.ruoyi.gserve.config;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.clickhouse.jdbc.ClickHouseDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ClickHouse 连接管理器（简化版）
 * <p>
 * 不使用 AbstractRoutingDataSource + AOP，改为直接管理连接。
 * 根据 project_id 从 game_project.clickhouse_config 读取配置，
 * 创建并缓存 ClickHouse DataSource。
 * <p>
 * 注意：首次运行时 project 配置还未加载，DataSource 按需创建。
 *
 * @author ruoyi
 */
@Configuration
public class ClickHouseConfig {

    private static final Logger log = LoggerFactory.getLogger(ClickHouseConfig.class);

    /** JDBC URL 模板 */
    private static final String JDBC_URL_TEMPLATE = "jdbc:clickhouse://%s:%d/%s";

    /** 数据源缓存（projectId -> DataSource） */
    private final Map<Long, DataSource> dataSourceCache = new ConcurrentHashMap<>();

    /**
     * 根据 project_id 获取 ClickHouse 连接
     */
    public Connection getConnection(Long projectId, String configJson) throws SQLException {
        DataSource ds = dataSourceCache.computeIfAbsent(projectId, pid -> {
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
    public boolean testConnection(String configJson) {
        try (Connection conn = createDataSource(configJson).getConnection()) {
            return conn.isValid(3);
        } catch (Exception e) {
            log.error("ClickHouse 连接测试失败", e);
            return false;
        }
    }

    /**
     * 根据配置 JSON 创建 DataSource
     */
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
        log.info("移除 ClickHouse 数据源: projectId={}", projectId);
    }
}
