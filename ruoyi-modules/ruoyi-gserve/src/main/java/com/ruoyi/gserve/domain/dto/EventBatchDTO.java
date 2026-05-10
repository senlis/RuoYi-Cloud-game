package com.ruoyi.gserve.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 游戏服上报数据 DTO
 * <p>
 * 游戏服通过 HTTP POST 批量上报事件数据，格式如下：
 * <pre>
 * {
 *   "table": "game_event_role_login",
 *   "appId": "huawei_app_001",
 *   "events": [{ "event_time": "...", "role_id": 1001, ... }]
 * }
 * </pre>
 *
 * @author ruoyi
 */
public class EventBatchDTO {

    /** 目标表名（如 game_event_role_login） */
    @NotBlank(message = "表名不能为空")
    private String table;

    /** 渠道 AppId（由 Gateway 注入，游戏服可不上报） */
    private String appId;

    /** 事件数据列表 */
    @NotNull(message = "事件数据不能为空")
    private List<EventEntry> events;

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public List<EventEntry> getEvents() {
        return events;
    }

    public void setEvents(List<EventEntry> events) {
        this.events = events;
    }

    /**
     * 单条事件数据（key-value 结构，字段名与 ClickHouse 列名一致）
     */
    public static class EventEntry {

        /** 事件时间（格式：yyyy-MM-dd HH:mm:ss） */
        @NotBlank(message = "event_time 不能为空")
        private String eventTime;

        /** 角色 ID */
        private Long roleId;

        /** 服务器 ID */
        private Long serverId;

        /** 扩展属性（key-value，与 ClickHouse 列名对应） */
        private java.util.Map<String, Object> fields;

        public String getEventTime() {
            return eventTime;
        }

        public void setEventTime(String eventTime) {
            this.eventTime = eventTime;
        }

        public Long getRoleId() {
            return roleId;
        }

        public void setRoleId(Long roleId) {
            this.roleId = roleId;
        }

        public Long getServerId() {
            return serverId;
        }

        public void setServerId(Long serverId) {
            this.serverId = serverId;
        }

        public java.util.Map<String, Object> getFields() {
            return fields;
        }

        public void setFields(java.util.Map<String, Object> fields) {
            this.fields = fields;
        }
    }
}
