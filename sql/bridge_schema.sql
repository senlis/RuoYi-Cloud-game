-- =========================================
-- 渠道桥接模块数据库脚本
-- =========================================



-- ----------------------------
-- 用户表（渠道用户绑定）
-- ----------------------------
DROP TABLE IF EXISTS br_user;
CREATE TABLE br_user (
    user_id         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    identity_id     VARCHAR(128) NOT NULL COMMENT '平台唯一标识',
    channel_key     VARCHAR(64)  NOT NULL COMMENT '渠道标识',
    channel_user_id VARCHAR(128) NOT NULL COMMENT '渠道用户ID',
    server_id       INT          DEFAULT NULL COMMENT '服务器ID',
    player_id       BIGINT       DEFAULT NULL COMMENT '游戏角色ID',
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    last_login_time DATETIME     DEFAULT NULL COMMENT '最后登录时间',
    PRIMARY KEY (user_id),
    UNIQUE KEY uk_identity_id (identity_id),
    KEY idx_channel_key (channel_key),
    KEY idx_channel_user (channel_key, channel_user_id, server_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='渠道用户表';

-- ----------------------------
-- 支付订单表
-- ----------------------------
DROP TABLE IF EXISTS br_pay_order;
CREATE TABLE br_pay_order (
    order_id          VARCHAR(64)   NOT NULL COMMENT '订单ID（业务主键）',
    channel_order_id  VARCHAR(128)  DEFAULT NULL COMMENT '渠道订单ID',
    channel_key       VARCHAR(64)   NOT NULL COMMENT '渠道标识',
    region_code       VARCHAR(128)  DEFAULT NULL COMMENT '分区CODE',
    server_id         INT           DEFAULT NULL COMMENT '服务器ID',
    player_id         BIGINT        DEFAULT NULL COMMENT '游戏角色ID',
    user_id           VARCHAR(64)   DEFAULT NULL COMMENT '用户标识',
    identity_id       VARCHAR(128)  DEFAULT NULL COMMENT '平台身份标识',
    product_id        VARCHAR(64)   DEFAULT NULL COMMENT '商品ID',
    product_name      VARCHAR(256)  DEFAULT NULL COMMENT '商品名称',
    amount            DECIMAL(18,2) NOT NULL COMMENT '金额',
    game_currency     INT           DEFAULT 0 COMMENT '游戏币数量',
    currency          VARCHAR(16)   DEFAULT 'CNY' COMMENT '货币类型',
    pay_status        INT           DEFAULT 0 COMMENT '支付状态（0初始 5确认兑换中 10兑换成功 15补单成功 20兑换失败）',
    exchange_url      VARCHAR(512)  DEFAULT NULL COMMENT '兑换地址',
    pay_way           VARCHAR(32)   DEFAULT NULL COMMENT '支付方式',
    equipment         VARCHAR(256)  DEFAULT NULL COMMENT '设备信息',
    remark            VARCHAR(500)  DEFAULT NULL COMMENT '备注',
    order_time        DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
    pay_time          DATETIME      DEFAULT NULL COMMENT '支付时间',
    create_time       DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time       DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (order_id),
    KEY idx_channel_order (channel_key, channel_order_id),
    KEY idx_order_time (order_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付订单表';



-- ----------------------------
-- 角色信息表（按渠道分库存储）
-- ----------------------------
DROP TABLE IF EXISTS br_role;
CREATE TABLE br_role (
    player_id        BIGINT       NOT NULL COMMENT '角色ID',
    channel_key      VARCHAR(64)  NOT NULL COMMENT '渠道KEY',
    region_code      VARCHAR(128) DEFAULT NULL COMMENT '分区CODE',
    server_id        INT          DEFAULT NULL COMMENT '服务器ID',
    account_id       VARCHAR(128) DEFAULT NULL COMMENT '账号ID',
    role_name        VARCHAR(128) DEFAULT NULL COMMENT '角色名',
    level            INT          DEFAULT 1 COMMENT '等级',
    vocation         INT          DEFAULT 0 COMMENT '职业',
    vip              INT          DEFAULT 0 COMMENT 'VIP等级',
    fight            BIGINT       DEFAULT 0 COMMENT '战斗力',
    create_time      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (player_id),
    KEY idx_account (channel_key, account_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色信息表';
