package com.ruoyi.gserve.strategy;

import java.util.List;
import java.util.Map;

/**
 * 事件写入策略接口
 * <p>
 * 支持多种写入方式：
 * - direct: 同步直写 ClickHouse（默认）
 * - async:  异步内存队列 + 定时批量写入
 * - mq:     消息队列（预留）
 *
 * @author ruoyi
 */
public interface EventWriteStrategy {

    /**
     * 写入事件数据
     *
     * @param projectId 项目ID
     * @param tableName 目标表名
     * @param columns   列名
     * @param rows      数据行
     * @return 写入成功条数（异步策略返回已入队数）
     */
    int write(long projectId, String tableName, List<String> columns, List<Map<String, Object>> rows);

    /** 策略名称 */
    String name();
}
