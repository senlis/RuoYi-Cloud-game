package com.ruoyi.gserve.service;

import com.ruoyi.gserve.config.ClickHouseConfig;
import com.ruoyi.gserve.config.GserveConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;

/**
 * ClickHouse 批量写入服务
 * <p>
 * 将校验通过的事件数据批量写入 ClickHouse。
 * 支持重试机制，写入异常记录到死信队列。
 *
 * @author ruoyi
 */
@Service
public class ClickHouseWriterService {

    private static final Logger log = LoggerFactory.getLogger(ClickHouseWriterService.class);

    private final ClickHouseConfig clickHouseConfig;
    private final GserveConfig gserveConfig;

    public ClickHouseWriterService(ClickHouseConfig clickHouseConfig, GserveConfig gserveConfig) {
        this.clickHouseConfig = clickHouseConfig;
        this.gserveConfig = gserveConfig;
    }

    /**
     * 批量写入数据
     *
     * @param projectId    项目 ID（用于路由 ClickHouse）
     * @param configJson   项目 ClickHouse 配置 JSON
     * @param tableName    目标表名
     * @param columns      列名列表
     * @param rows         数据行
     */
    public int batchWrite(long projectId, String configJson, String tableName,
                          List<String> columns, List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty()) {
            return 0;
        }

        int batchSize = gserveConfig.getDefaultBatchSize();
        int retries = gserveConfig.getRetry().getMaxRetries();
        int totalWritten = 0;

        for (int i = 0; i < rows.size(); i += batchSize) {
            int end = Math.min(i + batchSize, rows.size());
            List<Map<String, Object>> batch = rows.subList(i, end);

            boolean success = false;
            Exception lastException = null;

            for (int attempt = 0; attempt <= retries; attempt++) {
                try {
                    int n = doWrite(projectId, configJson, tableName, columns, batch);
                    totalWritten += n;
                    success = true;
                    break;
                } catch (Exception e) {
                    lastException = e;
                    log.warn("ClickHouse 写入失败(第{}次): table={}, rows={}, err={}",
                            attempt + 1, tableName, batch.size(), e.getMessage());
                    if (attempt < retries) {
                        try {
                            Thread.sleep(gserveConfig.getRetry().getRetryIntervalMs());
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }

            if (!success) {
                log.error("ClickHouse 写入最终失败: table={}, rows={}, err={}",
                        tableName, batch.size(),
                        lastException != null ? lastException.getMessage() : "unknown");
                // TODO: 写入死信队列 MySQL game_etl_error_log
            }
        }

        return totalWritten;
    }

    /**
     * 执行单次批量写入
     */
    private int doWrite(long projectId, String configJson, String tableName,
                        List<String> columns, List<Map<String, Object>> rows) throws Exception {

        // 构建占位符 SQL
        StringBuilder placeholders = new StringBuilder("(");
        for (int j = 0; j < columns.size(); j++) {
            if (j > 0) placeholders.append(", ");
            placeholders.append("?");
        }
        placeholders.append(")");

        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ").append(tableName).append(" (");
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) sql.append(", ");
            sql.append(columns.get(i));
        }
        sql.append(") VALUES ");
        for (int i = 0; i < rows.size(); i++) {
            if (i > 0) sql.append(", ");
            sql.append(placeholders);
        }

        try (Connection conn = clickHouseConfig.getConnection(projectId, configJson);
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            for (Map<String, Object> row : rows) {
                for (String col : columns) {
                    ps.setObject(idx++, row.getOrDefault(col, null));
                }
            }

            int count = ps.executeUpdate();
            log.debug("ClickHouse 写入成功: table={}, rows={}", tableName, count);
            return count;
        }
    }
}
