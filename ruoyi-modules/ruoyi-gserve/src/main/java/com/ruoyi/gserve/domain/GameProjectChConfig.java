package com.ruoyi.gserve.domain;

/**
 * 游戏项目 ClickHouse 配置（轻量实体）
 * <p>
 * 仅映射 game_project 表的 project_id 和 clickhouse_config 两个字段，
 * 供 gserve 模块读取 ClickHouse 连接信息。
 * 与 system 模块的 GameProject 共用同一张 MySQL 表但互不依赖。
 *
 * @author ruoyi
 */
public class GameProjectChConfig {

    /** 项目 ID */
    private Long projectId;

    /** ClickHouse 连接配置 JSON */
    private String clickhouseConfig;

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getClickhouseConfig() {
        return clickhouseConfig;
    }

    public void setClickhouseConfig(String clickhouseConfig) {
        this.clickhouseConfig = clickhouseConfig;
    }
}
