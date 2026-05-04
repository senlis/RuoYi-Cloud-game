-- ============================================
-- 游戏运营管理平台 - 数据库建表脚本
-- 基于 RuoYi-Cloud v3.6.8
-- ============================================

-- 1. 游戏项目表
CREATE TABLE IF NOT EXISTS game_project (
  project_id    bigint(20)   NOT NULL AUTO_INCREMENT  COMMENT '项目ID',
  project_code  varchar(50)  NOT NULL  COMMENT '项目编码(唯一)',
  project_name  varchar(100) NOT NULL  COMMENT '项目名称',
  status        char(1)      DEFAULT '0' COMMENT '状态(0正常 1停用)',
  sort          int(4)       DEFAULT 0  COMMENT '显示顺序',
  dynamic_fields text        COMMENT '动态字段值(JSON)',
  remark        varchar(500) COMMENT '备注',
  create_by     varchar(64)  DEFAULT '' COMMENT '创建者',
  create_time   datetime     COMMENT '创建时间',
  update_by     varchar(64)  DEFAULT '' COMMENT '更新者',
  update_time   datetime     COMMENT '更新时间',
  PRIMARY KEY (project_id),
  UNIQUE KEY uk_project_code (project_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='游戏项目表';

-- 2. 游戏渠道表
CREATE TABLE IF NOT EXISTS game_channel (
  channel_id     bigint(20)   NOT NULL AUTO_INCREMENT  COMMENT '渠道ID',
  project_id     bigint(20)   NOT NULL  COMMENT '所属项目ID',
  channel_code   varchar(50)  NOT NULL  COMMENT '渠道编码',
  channel_name   varchar(100) NOT NULL  COMMENT '渠道名称',
  channel_type   char(1)      DEFAULT '0' COMMENT '渠道类型(0=官方 1=联运 2=海外)',
  status         char(1)      DEFAULT '0' COMMENT '状态(0正常 1停用)',
  contact_person varchar(50)  COMMENT '联系人',
  contact_phone  varchar(20)  COMMENT '联系电话',
  sort           int(4)       DEFAULT 0  COMMENT '显示顺序',
  dynamic_fields text         COMMENT '动态字段值(JSON)',
  remark         varchar(500) COMMENT '备注',
  create_by      varchar(64)  DEFAULT '' COMMENT '创建者',
  create_time    datetime     COMMENT '创建时间',
  update_by      varchar(64)  DEFAULT '' COMMENT '更新者',
  update_time    datetime     COMMENT '更新时间',
  PRIMARY KEY (channel_id),
  UNIQUE KEY uk_channel_code (channel_code),
  KEY idx_project (project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='游戏渠道表';

-- 3. 游戏分区表
CREATE TABLE IF NOT EXISTS game_region (
  region_id        bigint(20)   NOT NULL AUTO_INCREMENT  COMMENT '分区ID',
  channel_id       bigint(20)   NOT NULL  COMMENT '所属渠道ID',
  project_id       bigint(20)   NOT NULL  COMMENT '所属项目ID(冗余)',
  region_code      varchar(50)  NOT NULL  COMMENT '分区编码',
  region_name      varchar(100) NOT NULL  COMMENT '分区名称',
  status           char(1)      DEFAULT '0' COMMENT '状态(0正常 1停用)',
  sort             int(4)       DEFAULT 0  COMMENT '显示顺序',
  proxy_region_key varchar(100) COMMENT '代理分区key：导出时直接使用该分区的配置',
  config           text         COMMENT '分区导出配置(JSON): region固定字段+可导出动态字段',
  servers          text         COMMENT '分区下服务器配置(JSON数组)',
  dynamic_fields   text         COMMENT '动态字段值(JSON)',
  remark           varchar(500) COMMENT '备注',
  create_by        varchar(64)  DEFAULT '' COMMENT '创建者',
  create_time      datetime     COMMENT '创建时间',
  update_by        varchar(64)  DEFAULT '' COMMENT '更新者',
  update_time      datetime     COMMENT '更新时间',
  PRIMARY KEY (region_id),
  KEY idx_channel (channel_id),
  KEY idx_project (project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='游戏分区表';

-- 4. 游戏服务器表（只关联分区，不直接关联项目/渠道）
CREATE TABLE IF NOT EXISTS game_server (
  id              bigint(20)   NOT NULL AUTO_INCREMENT  COMMENT '自增主键ID',
  server_id       int(11)      NOT NULL  COMMENT '服务器ID(数字编码)',
  region_id       bigint(20)   NOT NULL  COMMENT '所属分区ID',
  server_name     varchar(100) NOT NULL  COMMENT '服务器名称',
  server_type     char(1)      DEFAULT '0' COMMENT '服务器类型(0=正式 1=测试 2=体验)',
  status          char(1)      DEFAULT '0' COMMENT '状态(0=初始 1=待开 2=停服 3=维护 4=正常)',
  open_time       datetime     COMMENT '开服时间',
  backend_url     varchar(255) COMMENT '后台交互地址',
  game_db_config  text         COMMENT '游戏数据库配置(JSON)',
  log_db_config   text         COMMENT '游戏日志数据库配置(JSON)',
  merge_info      text         COMMENT '合服信息(JSON数组)',
  sort            int(4)       DEFAULT 0  COMMENT '显示顺序',
  dynamic_fields  text         COMMENT '动态字段值(JSON)',
  remark          varchar(500) COMMENT '备注',
  create_by       varchar(64)  DEFAULT '' COMMENT '创建者',
  create_time     datetime     COMMENT '创建时间',
  update_by       varchar(64)  DEFAULT '' COMMENT '更新者',
  update_time     datetime     COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_server_region (server_id, region_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='游戏服务器表';

-- 5. 动态字段定义表
CREATE TABLE IF NOT EXISTS game_field_define (
  field_id      bigint(20)   NOT NULL AUTO_INCREMENT  COMMENT '字段ID',
  entity_type   varchar(50)  NOT NULL  COMMENT '所属实体(project/channel/region/server)',
  field_label   varchar(100) NOT NULL  COMMENT '字段显示名称',
  field_code    varchar(100) NOT NULL  COMMENT '字段编码',
  field_type    varchar(50)  NOT NULL  COMMENT '字段类型(text/number/textarea/date/select/boolean)',
  field_options text         COMMENT '选项配置(JSON数组)',
  is_export     char(1)      DEFAULT 'N' COMMENT '是否导出(Y/N)',
  is_required   char(1)      DEFAULT 'N' COMMENT '是否必填',
  default_value varchar(500) COMMENT '默认值',
  sort          int(4)       DEFAULT 0  COMMENT '排序号',
  status        char(1)      DEFAULT '0' COMMENT '状态(0正常 1停用)',
  remark        varchar(500) COMMENT '备注',
  create_by     varchar(64)  DEFAULT '' COMMENT '创建者',
  create_time   datetime     COMMENT '创建时间',
  update_by     varchar(64)  DEFAULT '' COMMENT '更新者',
  update_time   datetime     COMMENT '更新时间',
  PRIMARY KEY (field_id),
  UNIQUE KEY uk_entity_field (entity_type, field_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态字段定义表';
