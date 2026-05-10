-- ============================================
-- 游戏数据统计分析 - MySQL 表结构迁移脚本
-- ============================================

-- ============================================
-- 1. game_project 表新增 clickhouse_config 字段
-- ============================================
ALTER TABLE game_project
    ADD COLUMN clickhouse_config text COMMENT 'ClickHouse 连接配置(JSON)';

-- 验证
-- SELECT project_id, project_code, project_name, clickhouse_config FROM game_project;


-- ============================================
-- 2. game_channel 表新增 secure_key 相关字段
-- ============================================
ALTER TABLE game_channel
    ADD COLUMN secure_key_hash      varchar(128) DEFAULT NULL COMMENT 'SecureKey SHA-256 哈希',
    ADD COLUMN secure_key_salt      varchar(64)  DEFAULT NULL COMMENT 'SecureKey 盐值',
    ADD COLUMN secure_key_version   int(11)      DEFAULT 0   COMMENT 'SecureKey 版本号(轮换时递增)',
    ADD COLUMN secure_key_updated_at datetime   DEFAULT NULL COMMENT 'SecureKey 最近更新时间';

-- 验证
-- SELECT channel_id, channel_code, channel_name, secure_key_hash, secure_key_version FROM game_channel;


-- ============================================
-- 3. game_event_type_config 表（事件类型配置）
-- ============================================
CREATE TABLE IF NOT EXISTS game_event_type_config (
    id            bigint(20)   NOT NULL AUTO_INCREMENT  COMMENT '自增ID',
    event_type    varchar(64)  NOT NULL  COMMENT '事件类型编码(如 pass)',
    event_name    varchar(128) NOT NULL  COMMENT '事件中文名(如 通关)',
    status        char(1)      DEFAULT '0' COMMENT '状态(0正常 1停用)',
    param_define  text         COMMENT '参数解析定义(JSON)',
    remark        varchar(512) COMMENT '备注说明',
    create_by     varchar(64)  DEFAULT '' COMMENT '创建者',
    create_time   datetime     COMMENT '创建时间',
    update_by     varchar(64)  DEFAULT '' COMMENT '更新者',
    update_time   datetime     COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_event_type (event_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='事件类型配置表 - 定义n1~n10/s1~s5的参数语义';


-- ============================================
-- 4. game_etl_error_log 表（ETL 死信队列）
-- ============================================
CREATE TABLE IF NOT EXISTS game_etl_error_log (
    id          bigint(20)   NOT NULL AUTO_INCREMENT  COMMENT '自增ID',
    app_id      varchar(128) NOT NULL  COMMENT '渠道 AppId',
    raw_data    longtext     NOT NULL  COMMENT '原始上报数据(JSON)',
    error_msg   varchar(1024) NOT NULL COMMENT '错误信息',
    event_type  varchar(64)  DEFAULT NULL COMMENT '事件类型',
    status      tinyint(4)   DEFAULT 0  COMMENT '状态(0=待处理 1=已重试 2=已忽略)',
    created_at  datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  datetime     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_status (status),
    KEY idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ETL 错误日志表(死信队列)';


-- ============================================
-- 5. 菜单初始化（数据统计分析相关菜单）
-- ============================================
-- 注意：请根据实际 parent_id 调整
/*
-- 数据统计顶级菜单
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
VALUES ('数据统计', 0, 5, 'analytics', NULL, 1, 0, 'M', '0', '0', NULL, 'chart', 'admin', sysdate());

-- 获取新菜单 ID
-- 统计分析仪表盘
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
VALUES ('统计分析', @parent_id, 1, 'analytics/dashboard', 'game/analytics/dashboard', 1, 0, 'C', '0', '0', 'game:analytics:list', 'chart', 'admin', sysdate());

-- 事件类型配置
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
VALUES ('事件类型', @parent_id, 2, 'analytics/event-type', 'game/analytics/eventType', 1, 0, 'C', '0', '0', 'game:analytics:eventType', 'edit', 'admin', sysdate());

-- 行为日志查询
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
VALUES ('玩家行为日志', @parent_id, 3, 'analytics/event-log', 'game/analytics/eventLog', 1, 0, 'C', '0', '0', 'game:analytics:eventLog', 'log', 'admin', sysdate());
*/
