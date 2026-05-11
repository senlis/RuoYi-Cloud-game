package com.ruoyi.gserve.strategy.model;

import java.util.List;
import java.util.Map;

/**
 * 缓冲事件（待写入队列的一条数据）
 *
 * @author ruoyi
 */
public class BufferedEvent {

    private final long projectId;
    private final String tableName;
    private final List<String> columns;
    private final Map<String, Object> row;

    public BufferedEvent(long projectId, String tableName, List<String> columns, Map<String, Object> row) {
        this.projectId = projectId;
        this.tableName = tableName;
        this.columns = columns;
        this.row = row;
    }

    public long getProjectId() { return projectId; }
    public String getTableName() { return tableName; }
    public List<String> getColumns() { return columns; }
    public Map<String, Object> getRow() { return row; }
}
