-- ----------------------------
-- 道具配置表（用于道具发放、GM邮件等功能中的快捷选择）
-- 按项目隔离，每个项目独立管理道具列表
-- ----------------------------
DROP TABLE IF EXISTS t_item_config;
CREATE TABLE t_item_config (
    project_id BIGINT       NOT NULL COMMENT '项目ID',
    item_id    BIGINT       NOT NULL COMMENT '道具ID（来自Excel导入）',
    item_name  VARCHAR(256) NOT NULL COMMENT '道具名称',
    created_at DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (project_id, item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='游戏道具配置表';
