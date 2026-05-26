-- ----------------------------
-- GM 邮件表（通知/奖励邮件）
-- 所有时间字段存储 UTC，页面按用户本地时区展示
-- ----------------------------
DROP TABLE IF EXISTS t_gm_mail;
CREATE TABLE t_gm_mail (
    mail_id        BIGINT       NOT NULL AUTO_INCREMENT COMMENT '邮件ID',
    title          VARCHAR(256) NOT NULL COMMENT '邮件标题',
    content        TEXT         COMMENT '邮件内容（富文本HTML）',
    send_type      TINYINT      DEFAULT 0 COMMENT '发送方式 0=立即 1=定时',
    send_time      DATETIME     COMMENT '定时发送时间(UTC)',
    expire_days    INT          DEFAULT 7 COMMENT '有效期天数',
    target_type    TINYINT      DEFAULT 0 COMMENT '目标范围 0=全服 1=条件筛选 2=指定玩家',
    target_players TEXT         COMMENT '指定角色ID列表(逗号分隔)',
    min_level      INT          COMMENT '最低等级',
    max_level      INT          COMMENT '最高等级',
    min_vip        INT          COMMENT '最低VIP',
    max_vip        INT          COMMENT '最高VIP',
    rewards        TEXT         COMMENT '附件奖励JSON [{itemId,itemName,count}]',
    status         TINYINT      DEFAULT 0 COMMENT '状态 0=待审核 1=审核通过 2=已发送 3=发送失败 4=驳回 5=已撤回',
    audit_remark   VARCHAR(256) COMMENT '驳回原因',
    server_ids     TEXT         COMMENT '目标服务器ID列表(逗号分隔)',
    failed_server_ids TEXT     COMMENT '推送失败服务器ID列表(逗号分隔)',
    channel_ids    TEXT         COMMENT '选中渠道ID列表(逗号分隔)',
    region_ids     TEXT         COMMENT '选中分区ID列表(逗号分隔)',
    project_id     BIGINT       COMMENT '所属项目',
    created_by     VARCHAR(64)  COMMENT '创建人',
    created_at     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (mail_id),
    KEY idx_status (status),
    KEY idx_project (project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='GM邮件表';
