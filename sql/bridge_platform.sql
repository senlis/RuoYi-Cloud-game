-- =============================================
-- 渠道接入平台配置 — 多库分片支持
-- =============================================

-- 1. 接入平台配置（每个渠道可独立配置数据库）
CREATE TABLE IF NOT EXISTS br_platform_config (
    platform_id      BIGINT       NOT NULL AUTO_INCREMENT COMMENT '平台ID',
    channel_key      VARCHAR(64)  NOT NULL COMMENT '渠道KEY',
    platform_name    VARCHAR(128) DEFAULT NULL COMMENT '平台名称',
    status           CHAR(1)      DEFAULT '0' COMMENT '平台状态(0正常 1停用)',
    recharge_status  CHAR(1)      DEFAULT '0' COMMENT '充值状态(0正常 1停用)',
    db_host          VARCHAR(255) DEFAULT NULL COMMENT '数据库地址',
    db_port          INT          DEFAULT 3306 COMMENT '数据库端口',
    db_name          VARCHAR(128) DEFAULT NULL COMMENT '数据库名',
    db_user          VARCHAR(64)  DEFAULT NULL COMMENT '数据库用户',
    db_pwd           VARCHAR(256) DEFAULT NULL COMMENT '数据库密码',
    remark           VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_time      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (platform_id),
    UNIQUE KEY uk_channel (channel_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='渠道接入平台配置';


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