-- ----------------------------
-- 内部资源申请表（GM工具）
-- 用于运营/QA/客服申请发放道具、调整等级等
-- ----------------------------
DROP TABLE IF EXISTS t_gm_resource_request;
CREATE TABLE t_gm_resource_request (
    request_id     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '申请单ID',
    title          VARCHAR(256) NOT NULL COMMENT '申请标题',
    request_type   TINYINT      DEFAULT 0 COMMENT '申请类型 0=道具 1=货币 2=等级 3=创建角色 4=自定义',
    project_id     BIGINT       COMMENT '项目ID',
    server_ids     TEXT         COMMENT '目标服务器ID列表(逗号分隔)',
    channel_ids    TEXT         COMMENT '选中渠道ID列表(逗号分隔)',
    region_ids     TEXT         COMMENT '选中分区ID列表(逗号分隔)',
    player_ids     TEXT         COMMENT '目标角色ID列表(逗号分隔)',
    resources      TEXT         COMMENT '资源内容JSON [{type,itemId,itemName,count,remark}]',
    reason         TEXT         COMMENT '申请理由',
    urgency        TINYINT      DEFAULT 0 COMMENT '紧急程度 0=普通 1=紧急',
    status         TINYINT      DEFAULT 0 COMMENT '状态 0=待审批 1=审批通过 2=已发放 3=发放失败 4=驳回 5=已撤回',
    applicant      VARCHAR(64)  COMMENT '申请人',
    approver       VARCHAR(64)  COMMENT '审批人',
    audit_remark   VARCHAR(256) COMMENT '审批意见',
    failed_server_ids TEXT     COMMENT '发放失败服务器ID列表',
    created_at     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (request_id),
    KEY idx_status (status),
    KEY idx_applicant (applicant)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内部资源申请表';
