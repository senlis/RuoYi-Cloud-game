-- =========================================
-- 渠道桥接模块数据库脚本
-- =========================================

-- ----------------------------
-- 渠道配置表
-- channel_key 唯一定位一组出包参数（渠道+发行平台）
-- 例如：huawei_overseas、huawei_cn、oppo_cn
-- ----------------------------
DROP TABLE IF EXISTS br_channel_arg_config;
CREATE TABLE br_channel_arg_config (
    channel_id      BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    channel_key     VARCHAR(64)  NOT NULL COMMENT '渠道唯一标识（来源于 game_channel.channel_code）',
    region_key      VARCHAR(128) NOT NULL COMMENT '分区KEY(来源于game_region.region_code)',
    platform_name   VARCHAR(64)  DEFAULT NULL COMMENT '发行平台（如 cn/overseas/hk）',
    package_name    VARCHAR(64)  DEFAULT NULL COMMENT '包名',
    app_id          VARCHAR(256) DEFAULT NULL COMMENT '渠道分配的AppID',
    app_key         TEXT         DEFAULT NULL COMMENT '渠道分配的AppKey',
    pay_key         TEXT         DEFAULT NULL COMMENT '支付公钥',
    auth_url        VARCHAR(512) DEFAULT NULL COMMENT 'SDK认证接口地址',
    package_params  TEXT         DEFAULT NULL COMMENT '其他出包参数(JSON)',
    status          CHAR(1)      DEFAULT '0' COMMENT '状态（0正常 1停用）',
    sort            INT(4)       DEFAULT 0  COMMENT '显示顺序',
    remark          VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (channel_id),
    UNIQUE KEY uk_channel_region (channel_key, region_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='渠道参数配置表';

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
