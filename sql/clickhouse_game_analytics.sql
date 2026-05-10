-- ============================================
-- 游戏数据统计分析 - ClickHouse 建表脚本
-- 数据库：ClickHouse (OLAP)
-- ============================================

-- ============================================
-- 1. 角色创角表
-- ============================================
CREATE TABLE IF NOT EXISTS game_statistics.game_event_chara_creation
(
    project_id     UInt32,
    channel_id     UInt32,
    region_id      UInt32,
    server_id      UInt32,
    event_time     DateTime,
    dt             Date,
    vip            UInt16,
    fight          UInt64,
    account_id     String,
    role_id        UInt64,
    role_name      String,
    level          UInt16,
    vocation       UInt16,
    gender         UInt8,
    device_id      String,
    device_os      String,
    ip             String,
    idfa           String,
    oaid           String,
    package_name   String,
    client_version String,
    report_time    DateTime,
    extra_json     String
)
ENGINE = MergeTree
PARTITION BY toYYYYMM(dt)
ORDER BY (server_id, dt, role_id)
TTL dt + INTERVAL 5 YEAR
COMMENT '角色创角表 - 新增用户锚点，用于留存/LTV计算';

-- ============================================
-- 2. 角色登录表
-- ============================================
CREATE TABLE IF NOT EXISTS game_statistics.game_event_role_login
(
    project_id      UInt32,
    channel_id      UInt32,
    region_id       UInt32,
    server_id       UInt32,
    event_time      DateTime,
    dt              Date,
    vip             UInt16,
    fight           UInt64,
    account_id      String,
    role_id         UInt64,
    role_name       String,
    level           UInt16,
    event_type      Enum8('login' = 1, 'logout' = 2),
    online_duration UInt32,
    ip              String,
    device_id       String,
    device_os       String,
    client_version  String,
    report_time     DateTime,
    extra_json      String
)
ENGINE = MergeTree
PARTITION BY toYYYYMM(dt)
ORDER BY (server_id, dt, role_id)
TTL dt + INTERVAL 12 MONTH
COMMENT '角色登录表 - DAU/在线时长统计';

-- ============================================
-- 3. 角色升级表
-- ============================================
CREATE TABLE IF NOT EXISTS game_statistics.game_event_role_levelup
(
    project_id           UInt32,
    channel_id           UInt32,
    region_id            UInt32,
    server_id            UInt32,
    event_time           DateTime,
    dt                   Date,
    vip                  UInt16,
    fight                UInt64,
    role_id              UInt64,
    role_name            String,
    old_level            UInt16,
    new_level            UInt16,
    chara_creation_dt    Date,
    cost_seconds         UInt32,
    total_online_seconds UInt32,
    vocation             UInt16,
    report_time          DateTime,
    extra_json           String
)
ENGINE = MergeTree
PARTITION BY toYYYYMM(dt)
ORDER BY (server_id, dt, role_id)
TTL dt + INTERVAL 5 YEAR
COMMENT '角色升级表 - 等级分布/升级速度分析';

-- ============================================
-- 4. 充值订单表
-- ============================================
CREATE TABLE IF NOT EXISTS game_statistics.game_event_recharge
(
    project_id         UInt32,
    channel_id         UInt32,
    region_id          UInt32,
    server_id          UInt32,
    event_time         DateTime,
    dt                 Date,
    vip                UInt16,
    fight              UInt64,
    account_id         String,
    role_id            UInt64,
    role_name          String,
    level              UInt16,
    order_id           String,
    channel_order_id   String,
    product_id         String,
    product_name       String,
    amount             Decimal(18, 2),
    pay_type           LowCardinality(String),
    currency           LowCardinality(String),
    order_status       Enum8('pending' = 0, 'success' = 1, 'refund' = 2),
    recharge_type      LowCardinality(String),
    is_first_recharge  UInt8,
    chara_creation_dt  Date,
    ip                 String,
    device_id          String,
    report_time        DateTime,
    extra_json         String
)
ENGINE = ReplacingMergeTree(event_time)
PARTITION BY toYYYYMM(dt)
ORDER BY (server_id, dt, role_id)
TTL dt + INTERVAL 5 YEAR
COMMENT '充值订单表 - 付费分析/LTV计算，order_id去重';

-- ============================================
-- 5. 玩家行为事件表
-- ============================================
CREATE TABLE IF NOT EXISTS game_statistics.game_event_role_event
(
    project_id      UInt32,
    channel_id      UInt32,
    region_id       UInt32,
    server_id       UInt32,
    event_time      DateTime,
    dt              Date,
    role_id         UInt64,
    role_name       String,
    account_id      String,
    vip             UInt16,
    fight           UInt64,
    level           UInt16,
    vocation        UInt16,
    event_type      LowCardinality(String),
    event_id        UInt32,
    event_value     Float64,
    n1              Int64,
    n2              Int64,
    n3              Int64,
    n4              Int64,
    n5              Int64,
    n6              Int64,
    n7              Int64,
    n8              Int64,
    n9              Int64,
    n10             Int64,
    s1              String,
    s2              String,
    s3              String,
    s4              String,
    s5              String,
    duration_ms     UInt32,
    client_version  String,
    device_id       String,
    ip              String,
    trace_id        String,
    report_time     DateTime,
    extra_json      String
)
ENGINE = MergeTree
PARTITION BY toYYYYMM(dt)
ORDER BY (server_id, event_type, dt, role_id)
TTL dt + INTERVAL 6 MONTH
COMMENT '玩家行为事件表 - 通用行为日志，event_type前置排序';

-- ============================================
-- 6. 道具变化记录表
-- ============================================
CREATE TABLE IF NOT EXISTS game_statistics.game_event_item_change
(
    project_id     UInt32,
    channel_id     UInt32,
    region_id      UInt32,
    server_id      UInt32,
    event_time     DateTime,
    dt             Date,
    vip            UInt16,
    fight          UInt64,
    role_id        UInt64,
    role_name      String,
    level          UInt16,
    opt_id         Int32,
    opt_type       Int32,
    item_id        UInt32,
    item_name      String,
    item_num       Int64,
    before_num     Int64,
    after_num      Int64,
    other_content  String,
    report_time    DateTime,
    extra_json     String
)
ENGINE = MergeTree
PARTITION BY toYYYYMM(dt)
ORDER BY (server_id, dt, role_id)
TTL dt + INTERVAL 6 MONTH
COMMENT '道具变化记录表 - 道具经济分析';