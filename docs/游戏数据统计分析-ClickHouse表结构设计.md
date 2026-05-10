# 游戏数据统计分析 — ClickHouse 表结构设计

> 版本：v4.0（需求修订版）
> 日期：2026-05-06
> 修订内容：1) ClickHouse 配置移至项目管理页面(game_project.clickhouse_config)，不在 Nacos 管理；2) gserve 模块只做数据接收不做查询；3) 统计查询统一在 system 模块 AnalyticsQueryController 实现
> 数据库：ClickHouse（列式存储 OLAP）

---

## 1. 背景与目标

基于现有游戏管理后台（项目/渠道/分区/服务器管理），新增数据统计分析模块。数据来源为各游戏服务器上报的玩家行为日志，经过 ETL 后落地 ClickHouse，提供多维度的游戏运营数据查询与报表支撑。

### 核心分析指标

| 维度 | 核心指标 |
|------|---------|
| 用户规模 | 新增创角数、DAU、MAU、WAU |
| 留存 | 次日/3日/7日/14日/30日留存 |
| 付费 | 付费率、ARPU、ARPPU、LTV、首充率 |
| 等级 | 等级分布、升级速度、等级流失 |
| 行为 | 玩法参与率、关卡通过率 |
| 道具经济 | 道具产出/消耗、资源流转 |
| 营收 | 日/周/月流水、渠道对比 |

---

## 2. ClickHouse 设计核心原则

### 2.1 引擎选择

| 表类型 | 推荐引擎 | 理由 |
|--------|---------|------|
| 事件流水表（创角/登录/升级/事件） | `MergeTree` | 纯追加写入，无更新需求 |
| 充值订单表 | `ReplacingMergeTree` | 订单状态可能变更（成功→退款），以 `order_id` 去重 |
| 道具变化记录表 | `MergeTree` | 纯追加写入 |
| 维度配置表 | MySQL 表（经由 JDBC 桥接）或 外部字典 | 不频繁变动，在 MySQL 中管理 |

### 2.2 分区策略

- **核心事件表**：按月分区 `toYYYYMM(dt)`，跨度一般不超过 6 个月可查，历史数据通过 TTL 自动淘汰
- **充值表**：按月分区，保留 12 个月以上（财务需求）

### 2.3 排序键设计原则

排序键决定 ClickHouse 存储的物理顺序，直接影响查询性能。设计原则：

1. **高基数的等值过滤条件优先放在前面**（如 `server_id`）
2. **时间范围过滤次之**（`dt` / `toDate(event_time)`）
3. **业务实体 ID 放最后**（如 `role_id`）

行为事件表例外：`event_type` 放在 `dt` 之前，因为典型查询是按事件类型过滤的跨天扫描。

### 2.4 类型映射说明

设计中的 ClickHouse 类型与用户熟悉的 MySQL 类型对应关系如下：

| ClickHouse 类型 | 对标 MySQL 类型 | 说明 |
|----------------|----------------|------|
| `Int8` | `tinyint(4)` | 小范围整数 |
| `Int32` | `int(11)` | 常规整数 |
| `Int64` | `bigint(20)` | 大整数 |
| `Float64` | `double` | 浮点数 |
| `Decimal(18,2)` | `decimal(18,2)` | 精确小数 |
| `String` | `varchar` / `text` | 字符串，ClickHouse 中无长度限制 |
| `Date` | `date` | 日期 |
| `DateTime` | `datetime` | 日期时间 |
| `Enum8` | `enum` | 枚举（内部编码为 Int8） |
| `LowCardinality(String)` | — | 字典编码字符串，过滤性能好 |

---

## 3. 表结构详设

### 3.1 公共字段约定

**所有事件表**统一包含以下公共字段（`game_event_role_event` 除外，该表不含 `vip` / `fight`）：

| 字段名 | ClickHouse 类型 | 对标 MySQL | 说明 |
|--------|----------------|------------|------|
| `project_id` | `UInt32` | `int(11)` | 项目 ID |
| `channel_id` | `UInt32` | `int(11)` | 渠道 ID |
| `region_id` | `UInt32` | `int(11)` | 区服 ID |
| `server_id` | `UInt32` | `int(11)` | 游戏服务器 ID |
| `event_time` | `DateTime` | `datetime` | 事件发生时间（服务器时间） |
| `dt` | `Date` | `date` | 事件日期（`toDate(event_time)`，冗余分区字段） |
| `vip` | `UInt16` | `int(11)` | **VIP 等级**（`game_event_role_event` 除外） |
| `fight` | `UInt64` | `bigint(20)` | **战力/游戏值**（`game_event_role_event` 除外） |
| `report_time` | `DateTime` | `datetime` | 日志上报时间（ETL 写入时间） |

`dt` 冗余设计目的：分区表达式避免函数计算，提高分区裁剪效率；同时也是物化视图的常用分组维度。

> 注意：`vip` 和 `fight` 为玩家在事件发生时刻的快照值，由游戏服务端随事件上报。`game_event_role_event` 不包含这两个字段，因为该表的行为事件类型广泛，非所有事件都需要战力上下文。

---

### 3.2 角色创角表 — `game_event_chara_creation`

**用途**：记录玩家创建角色的行为，是新增用户的第一笔记录，也是计算留存、LTV 的基础锚点。

**引擎**：`MergeTree`
**分区键**：`toYYYYMM(dt)`
**排序键**：`(server_id, dt, role_id)`

| # | 字段名 | 类型 | MySQL 对标 | 必需 | 说明 |
|---|--------|------|------------|------|------|
| 1-9 | 公共字段 9 个 | — | — | Y | project_id ~ fight |
| 10 | `account_id` | `String` | `varchar(128)` | Y | 平台账号 ID |
| 11 | `role_id` | `UInt64` | `bigint(20)` | Y | 角色 ID（游戏内唯一） |
| 12 | `role_name` | `String` | `varchar(64)` | Y | 角色名 |
| 13 | `level` | `UInt16` | `int(11)` | Y | 创角初始等级（通常为 1） |
| 14 | `vocation` | `UInt16` | `int(11)` | N | 职业/门派 ID |
| 15 | `gender` | `UInt8` | `tinyint(4)` | N | 性别（0=未知, 1=男, 2=女） |
| 16 | `device_id` | `String` | `varchar(128)` | N | 设备 ID |
| 17 | `device_os` | `String` | `varchar(32)` | N | 操作系统（iOS/Android） |
| 18 | `ip` | `String` | `varchar(64)` | N | 注册 IP |
| 19 | `idfa` | `String` | `varchar(128)` | N | iOS IDFA |
| 20 | `oaid` | `String` | `varchar(128)` | N | Android OAID |
| 21 | `package_name` | `String` | `varchar(128)` | N | 包名/渠道包标识 |
| 22 | `client_version` | `String` | `varchar(32)` | N | 客户端版本 |
| 23 | `report_time` | `DateTime` | `datetime` | Y | 上报时间 |
| 24 | `extra_json` | `String` | `text` | N | 额外扩展信息（JSON） |

**补充说明**：
- 创角表是留存计算、LTV 计算的锚点表，必须有精确的 `event_time`
- 同一 `role_id` 重复上报时以 `event_time` 最早的为准做去重

**典型查询**：

```sql
-- 按日统计新增创角数
SELECT dt, server_id, count(DISTINCT role_id) AS new_roles
FROM game_event_chara_creation
WHERE dt BETWEEN '2026-05-01' AND '2026-05-07'
GROUP BY dt, server_id;
```

---

### 3.3 角色登录表 — `game_event_role_login`

**用途**：记录玩家每日登录/登出行为，是计算 DAU、在线时长、留存的核心数据源。

**引擎**：`MergeTree`
**分区键**：`toYYYYMM(dt)`
**排序键**：`(server_id, dt, role_id)`

| # | 字段名 | 类型 | 对标 MySQL | 必需 | 说明 |
|---|--------|------|------------|------|------|
| 1-9 | 公共字段 9 个 | — | — | Y | project_id ~ fight |
| 10 | `account_id` | `String` | `varchar(128)` | Y | 平台账号 ID |
| 11 | `role_id` | `UInt64` | `bigint(20)` | Y | 角色 ID |
| 12 | `role_name` | `String` | `varchar(64)` | Y | 角色名 |
| 13 | `level` | `UInt16` | `int(11)` | Y | 登录时角色等级 |
| 14 | `event_type` | `Enum8('login'=1, 'logout'=2)` | — | Y | 事件类型 |
| 15 | `online_duration` | `UInt32` | `int(11)` | N | 本次在线时长（秒），仅登出时有值 |
| 16 | `ip` | `String` | `varchar(64)` | N | 登录 IP |
| 17 | `device_id` | `String` | `varchar(128)` | N | 设备 ID |
| 18 | `device_os` | `String` | `varchar(32)` | N | 操作系统 |
| 19 | `client_version` | `String` | `varchar(32)` | N | 客户端版本 |
| 20 | `report_time` | `DateTime` | `datetime` | Y | 上报时间 |
| 21 | `extra_json` | `String` | `text` | N | 扩展信息（JSON） |

**典型查询**：

```sql
-- DAU
SELECT dt, server_id, count(DISTINCT role_id) AS dau
FROM game_event_role_login
WHERE event_type = 'login' AND dt BETWEEN '2026-05-01' AND '2026-05-07'
GROUP BY dt, server_id;

-- 日均在线时长（秒）
SELECT dt, server_id, avg(online_duration) AS avg_online_sec
FROM game_event_role_login
WHERE event_type = 'logout' AND online_duration > 0
  AND dt BETWEEN '2026-05-01' AND '2026-05-07'
GROUP BY dt, server_id;
```

---

### 3.4 角色升级表 — `game_event_role_levelup`

**用途**：记录角色等级变化，用于分析等级分布、升级速度、各等级流失情况。

**引擎**：`MergeTree`
**分区键**：`toYYYYMM(dt)`
**排序键**：`(server_id, dt, role_id)`

| # | 字段名 | 类型 | 对标 MySQL | 必需 | 说明 |
|---|--------|------|------------|------|------|
| 1-9 | 公共字段 9 个 | — | — | Y | project_id ~ fight |
| 10 | `role_id` | `UInt64` | `bigint(20)` | Y | 角色 ID |
| 11 | `old_level` | `UInt16` | `int(11)` | Y | 升级前等级 |
| 12 | `new_level` | `UInt16` | `int(11)` | Y | 升级后等级 |
| 13 | `chara_creation_dt` | `Date` | `date` | N | 创角日期（冗余，免 JOIN） |
| 14 | `cost_seconds` | `UInt32` | `int(11)` | N | 距上次升级耗时（秒） |
| 15 | `total_online_seconds` | `UInt32` | `int(11)` | N | 累计在线时长（秒） |
| 16 | `vocation` | `UInt16` | `int(11)` | N | 职业（冗余） |
| 17 | `report_time` | `DateTime` | `datetime` | Y | 上报时间 |
| 18 | `extra_json` | `String` | `text` | N | 扩展信息 |

**补充说明**：`chara_creation_dt` 和 `vocation` 是从创角表冗余过来的字段，目的是避免跨表 JOIN。ClickHouse 的 JOIN 性能较差，能冗余就冗余。

**典型查询**：

```sql
-- 当前等级分布
SELECT new_level, count(DISTINCT role_id) AS role_count
FROM game_event_role_levelup
WHERE server_id = 1001 AND dt >= '2026-05-01'
GROUP BY new_level ORDER BY new_level;
```

---

### 3.5 充值订单表 — `game_event_recharge`

**用途**：记录玩家的充值订单，是收入分析的核心数据源，支持订单级别的精确查询。

**引擎**：`ReplacingMergeTree(event_time)`

> 以 `order_id` 去重。订单状态变更时使用相同 `order_id` + 新的 `event_time` 重新插入，异步合并保留最新版本。

**分区键**：`toYYYYMM(dt)`
**排序键**：`(server_id, dt, role_id)`

| # | 字段名 | 类型 | 对标 MySQL | 必需 | 说明 |
|---|--------|------|------------|------|------|
| 1-9 | 公共字段 9 个 | — | — | Y | project_id ~ fight |
| 10 | `account_id` | `String` | `varchar(128)` | Y | 平台账号 ID |
| 11 | `role_id` | `UInt64` | `bigint(20)` | Y | 角色 ID |
| 12 | `role_name` | `String` | `varchar(64)` | N | 角色名 |
| 13 | `level` | `UInt16` | `int(11)` | N | 下单时等级 |
| 14 | `order_id` | `String` | `varchar(128)` | Y | **游戏订单号（去重唯一键）** |
| 15 | `channel_order_id` | `String` | `varchar(128)` | N | 渠道订单号 |
| 16 | `product_id` | `String` | `varchar(64)` | Y | 商品 ID |
| 17 | `product_name` | `String` | `varchar(128)` | N | 商品名称 |
| 18 | `amount` | `Decimal(18,2)` | `decimal(18,2)` | Y | **充值金额（元）** |
| 19 | `pay_type` | `LowCardinality(String)` | `varchar(32)` | N | 支付方式 |
| 20 | `currency` | `LowCardinality(String)` | `varchar(8)` | N | 货币类型（CNY/USD） |
| 21 | `order_status` | `Enum8('pending'=0, 'success'=1, 'refund'=2)` | `tinyint(4)` | Y | 订单状态 |
| 22 | `recharge_type` | `LowCardinality(String)` | `varchar(32)` | N | 充值类型（first/normal/monthly_card/gift） |
| 23 | `is_first_recharge` | `UInt8` | `tinyint(4)` | N | 是否首充（0/1） |
| 24 | `chara_creation_dt` | `Date` | `date` | N | 创角日期（冗余，用于 LTV 免 JOIN） |
| 25 | `ip` | `String` | `varchar(64)` | N | 下单 IP |
| 26 | `device_id` | `String` | `varchar(128)` | N | 设备 ID |
| 27 | `report_time` | `DateTime` | `datetime` | Y | 上报时间 |
| 28 | `extra_json` | `String` | `text` | N | 扩展信息（JSON） |

**典型查询**：

```sql
-- 日流水
SELECT dt, server_id, sum(amount) AS revenue, count(DISTINCT role_id) AS paying_users
FROM game_event_recharge
WHERE order_status = 'success' AND dt BETWEEN '2026-05-01' AND '2026-05-07'
GROUP BY dt, server_id;
```

---

### 3.6 玩家行为事件表 — `game_event_role_event`

**用途**：通用的玩家行为日志记录表，覆盖游戏内除上述核心事件外的所有行为（副本进入/通关、任务完成/放弃、抽卡、PVP、交互等）。这是**数据量最大**的一张表。

**引擎**：`MergeTree`
**分区键**：`toYYYYMM(dt)`
**排序键**：`(server_id, event_type, dt, role_id)`

> 排序键将 `event_type` 放在 `dt` 之前：因为行为事件的典型查询是按事件类型过滤跨天数据，如"查所有玩家的抽卡记录"。`event_type` 在前可大幅减少扫描范围。

**通用参数说明**：本表提供 `n1~n10`（数值参数）和 `s1~s5`（字符串参数）共 15 个通用字段，各字段的语义由 `event_type` 决定，在后台事件类型配置页面中定义。

#### 3.6.1 字段定义

| # | 字段名 | 类型 | 对标 MySQL | 必需 | 说明 |
|---|--------|------|------------|------|------|
| 1-7 | 公共字段 7 个 | — | — | Y | project_id ~ dt |
| 8 | `role_id` | `UInt64` | `bigint(20)` | Y | 角色 ID |
| 9 | `account_id` | `String` | `varchar(128)` | N | 平台账号 ID |
| 10 | `level` | `UInt16` | `int(11)` | N | 事件发生时等级 |
| 11 | `vocation` | `UInt16` | `int(11)` | N | 职业 |
| 12 | `event_type` | `LowCardinality(String)` | `varchar(64)` | Y | **事件类型编码**（如 `pass`、`enter_instance`、`draw_card`） |
| 13 | `event_id` | `UInt32` | `int(11)` | N | 子类型 ID（如关卡 ID、卡池 ID） |
| 14 | `event_value` | `Float64` | `double` | N | 事件数值（如通关评分、抽卡次数） |
| 15 | `n1` | `Int64` | `int(11)` | N | 通用数值参数 1 |
| 16 | `n2` | `Int64` | `bigint(20)` | N | 通用数值参数 2 |
| 17 | `n3` | `Int64` | `bigint(20)` | N | 通用数值参数 3 |
| 18 | `n4` | `Int64` | `bigint(20)` | N | 通用数值参数 4 |
| 19 | `n5` | `Int64` | `bigint(20)` | N | 通用数值参数 5 |
| 20 | `n6` | `Int64` | `bigint(20)` | N | 通用数值参数 6 |
| 21 | `n7` | `Int64` | `bigint(20)` | N | 通用数值参数 7 |
| 22 | `n8` | `Int64` | `bigint(20)` | N | 通用数值参数 8 |
| 23 | `n9` | `Int64` | `bigint(20)` | N | 通用数值参数 9 |
| 24 | `n10` | `Int64` | `bigint(20)` | N | 通用数值参数 10 |
| 25 | `s1` | `String` | `varchar(4098)` | N | 通用字符串参数 1 |
| 26 | `s2` | `String` | `varchar(4098)` | N | 通用字符串参数 2 |
| 27 | `s3` | `String` | `varchar(4098)` | N | 通用字符串参数 3 |
| 28 | `s4` | `String` | `text` | N | 通用字符串参数 4（长文本） |
| 29 | `s5` | `String` | `text` | N | 通用字符串参数 5（长文本） |
| 30 | `duration_ms` | `UInt32` | `int(11)` | N | 事件耗时（毫秒） |
| 31 | `client_version` | `String` | `varchar(32)` | N | 客户端版本 |
| 32 | `device_id` | `String` | `varchar(128)` | N | 设备 ID |
| 33 | `ip` | `String` | `varchar(64)` | N | 事件 IP |
| 34 | `trace_id` | `String` | `varchar(128)` | N | 链路追踪 ID（关联同一流程的多个事件） |
| 35 | `report_time` | `DateTime` | `datetime` | Y | 上报时间 |
| 36 | `extra_json` | `String` | `text` | N | 兜底扩展信息（JSON） |

#### 3.6.2 参数语义示例

各字段的语义由 `event_type` 在后台配置决定。以下为示例：

| 事件类型 | event_type | n1 | n2 | n3 | s1 | s2 |
|---------|-----------|----|----|----|----|----|
| 通关 | `pass` | 关卡 ID | 难度 | 是否胜利 (0/1) | — | — |
| 进入副本 | `enter_instance` | 副本 ID | 难度 | — | — | — |
| 抽卡 | `draw_card` | 卡池 ID | 抽取次数 | — | 卡池名称 | 结果列表 |
| 任务完成 | `quest_complete` | 任务 ID | 任务类型 | — | 任务名称 | — |
| PVP 对战 | `pvp_battle` | 对手 ID | 是否胜利 (0/1) | 积分变化 | 对手名 | — |

#### 3.6.3 典型查询

```sql
-- 某关卡的通关次数（按日）
SELECT dt, n1 AS level_id,
       count(DISTINCT role_id) AS players,
       count() AS attempts,
       countIf(n3 = 1) AS victories
FROM game_event_role_event
WHERE event_type = 'pass' AND dt BETWEEN '2026-05-01' AND '2026-05-07'
GROUP BY dt, n1;

-- 某抽卡活动参与人数
SELECT dt, count(DISTINCT role_id) AS participants
FROM game_event_role_event
WHERE event_type = 'draw_card' AND n1 = 101
  AND dt BETWEEN '2026-05-01' AND '2026-05-07'
GROUP BY dt;
```

---

### 3.7 道具变化记录表 — `game_event_item_change`

**用途**：独立记录玩家的道具/资源变化（获得、消耗、合成、出售等）。与行为事件表分开的原因是道具变化数据量通常很大，独立存储便于按角色和道具维度做经济系统分析。

**引擎**：`MergeTree`
**分区键**：`toYYYYMM(dt)`
**排序键**：`(server_id, dt, role_id)`

| # | 字段名 | 类型 | 对标 MySQL | 必需 | 说明 |
|---|--------|------|------------|------|------|
| 1-9 | 公共字段 9 个 | — | — | Y | project_id ~ fight |
| 10 | `role_id` | `UInt64` | `bigint(20)` | Y | 角色 ID |
| 11 | `level` | `UInt16` | `int(11)` | N | 变动时等级 |
| 12 | `opt_id` | `Int32` | `int(11)` | N | 操作编码（什么行为导致的变化） |
| 13 | `opt_type` | `Int32` | `int(11)` | N | 操作类型分类 |
| 14 | `item_id` | `UInt32` | `int(11)` | N | 道具/资源 ID |
| 15 | `item_name` | `String` | `varchar(256)` | N | 道具名称 |
| 16 | `item_num` | `Int64` | `bigint(20)` | N | **变化数量（正=获得，负=消耗）** |
| 17 | `before_num` | `Int64` | `bigint(20)` | N | 变动前数量 |
| 18 | `after_num` | `Int64` | `bigint(20)` | N | 变动后数量 |
| 19 | `other_content` | `String` | `varchar(256)` | N | 其他说明 |
| 20 | `report_time` | `DateTime` | `datetime` | Y | 上报时间 |
| 21 | `extra_json` | `String` | `text` | N | 扩展信息（JSON） |

**补充说明**：
- 与行为事件表通过 `(role_id, event_time, opt_id)` 的联合可做关联分析（如"通关副本获得了哪些道具"）
- `item_num` 为正数表示获得，负数表示消耗，这种设计可以避免额外字段区分收入/支出
- 对于玩家背包中存量巨大的道具（如金币），`before_num` / `after_num` 记录的绝对值可能很大，所以要使用 `Int64`

**典型查询**：

```sql
-- 按日统计某道具的产出/消耗
SELECT dt,
       sumIf(item_num, item_num > 0) AS total_output,
       sumIf(abs(item_num), item_num < 0) AS total_consume,
       count(DISTINCT role_id) AS affected_players
FROM game_event_item_change
WHERE item_id = 1001 AND dt BETWEEN '2026-05-01' AND '2026-05-07'
GROUP BY dt;

-- 统计某个玩家在某个副本玩法中的道具产出
SELECT i.item_name, i.item_id, sum(i.item_num) AS total
FROM game_event_item_change i
WHERE i.role_id = 123456 AND i.opt_id = 101 AND i.dt = '2026-05-01'
GROUP BY i.item_id, i.item_name;
```

---

## 4. 表关系概览

```
game_event_chara_creation  ── 创角锚点（永久保留，留存/LTV 基础）
       │
       │ role_id 为主线
       v
game_event_role_login    ── DAU / 在线时长
game_event_role_levelup  ── 等级分布 / 升级速度
game_event_role_event    ── 通用行为 (n1~n10 + s1~s5)        【不含 vip/fight】
game_event_item_change   ── 道具经济 (与 role_event 通过 opt_id 关联)
game_event_recharge      ── 付费分析 (order_id 去重)
```

- **事实表之间不建外键**。ClickHouse 不适合 JOIN，通过冗余关键字段来避免 JOIN
- 维度信息（角色名、等级、VIP、战力等）随事件冗余存储，以事件发生时的快照值为准
- 所有表（除 `game_event_role_event`）均包含 `vip`（UInt16）和 `fight`（UInt64）两个玩家状态字段

---

## 5. 数据生命周期（TTL）

| 表名 | 热数据保留 | 总保留期 | 说明 |
|------|-----------|---------|------|
| `game_event_role_event` | 1 个月 | 6 个月 | 行为事件量最大，6 个月后清理 |
| `game_event_item_change` | 1 个月 | 6 个月 | 道具数据量大，与行为事件同步 |
| `game_event_role_login` | 3 个月 | 12 个月 | 登录数据是留存和 DAU 计算必需 |
| `game_event_role_levelup` | 6 个月 | 永久 | 升级数据量小 |
| `game_event_chara_creation` | 永久 | 永久 | 创角是留存锚点 |
| `game_event_recharge` | 12 个月 | 永久 | 财务对账必需 |

永久保留的表通过 ClickHouse 多级存储策略（SSD 热 + HDD 冷）降低成本，无需物理删除。

---

## 6. 事件类型配置管理

### 6.1 概述

`game_event_role_event` 表的 `event_type` 由游戏服务端自由上报，但后台管理端需要提供一个**事件类型解析配置页面**，用于定义各事件类型各字段的语义，以便在前端查询展示时做可读化解析。

### 6.2 配置表结构（MySQL 表）

事件类型元数据表 `game_event_type_config`，存储在业务 MySQL 数据库中，由后台管理页面维护：

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | `bigint(20)` PK | 自增 ID |
| `event_type` | `varchar(64)` UNIQUE | 事件类型编码（如 `pass`） |
| `event_name` | `varchar(128)` | 事件中文名（如"通关"） |
| `status` | `tinyint(4)` | 启用状态 0/1 |
| `param_define` | `text` | **参数解析定义（JSON）** |
| `remark` | `varchar(512)` | 备注说明 |
| `created_at` | `datetime` | 创建时间 |
| `updated_at` | `datetime` | 更新时间 |

### 6.3 参数解析定义格式

`param_define` 字段存储 JSON，定义 `n1~n10` 和 `s1~s5` 各字段的语义标签和值的解析规则：

```json
{
  "n1": { "label": "关卡ID", "type": "number" },
  "n2": { "label": "难度", "type": "number" },
  "n3": {
    "label": "是否胜利",
    "type": "enum",
    "enumValues": {
      "0": "失败",
      "1": "胜利"
    }
  }
}
```

当参数值：n1=1，n2=2,n3=1时，解析后的日志：关卡ID：1，难度:2,是否胜利:胜利

### 6.4 后台配置页面功能

1. **事件类型列表**：展示所有已注册的 `event_type`，支持搜索和筛选
2. **新增/编辑事件类型**：填写 `event_type` 编码、名称、启用状态
3. **参数解析配置**：用表单方式定义 `n1~n10` / `s1~s5` 各字段的标签、类型（数字/字符串/枚举）、枚举映射值
4. **事件类型启用/停用**：停用后前端查询页面不展示该事件类型
5. **备注说明**：用于描述该事件的触发时机和业务含义

### 6.5 玩家行为日志查询页面

利用上述配置，后台提供一个**玩家行为日志查询页面**，可按 `role_id`、`event_type`、时间范围等条件查询原始事件，展示时根据 `param_define` 配置将 `n1~n10` / `s1~s5` 渲染为可读化的字段标签和值（如 `n3=1` 渲染为"胜利"）。

---

## 7. gserve 模块与 SecureKey 认证设计

### 7.1 新增 gserve 模块概述

新增独立模块 `ruoyi-modules/ruoyi-gserve/`，专门处理游戏数据上报、ETL 写入和数据查询服务。该模块独立可部署，支持多实例水平扩展。

**模块定位**：

```
┌─────────────────────────────────────────────────────────┐
│                   客户端请求入口                          │
│    Nginx(:80) → Gateway(:8080)                           │
│       ├── /gserve/**  →  SecureKey 认证 →  gserve 服务   │
│       └── /system/**  →  JWT 认证     →  system 服务     │
└─────────────────────────────────────────────────────────┘
```

**gserve 模块职责**：
1. **数据接收**：接收游戏服通过 HTTP 上报的行为日志数据
2. **SecureKey 认证**：验证请求签名的合法性（由 Gateway 完成）
3. **数据校验**：校验上报数据的必填字段和格式
4. **ClickHouse 写入**：将校验通过的数据批量写入 ClickHouse

> 注：gserve 模块**不包含**统计查询功能。查询分析统一在 `ruoyi-system` 模块的 `AnalyticsQueryController` 中实现。

**模块结构**：

```
ruoyi-gserve/
├── src/main/java/com/ruoyi/gserve/
│   ├── GserveApplication.java
│   ├── controller/
│   │   └── EventReceiveController.java    # 数据接收接口（只做 ETL 接收）
│   ├── service/
│   │   ├── EventReceiveService.java        # 数据接收与校验
│   │   ├── ClickHouseWriterService.java    # ClickHouse 批量写入
│   │   └── SecureKeyService.java           # SecureKey 验证服务
│   ├── config/
│   │   ├── ClickHouseConfig.java           # ClickHouse 连接配置
│   │   └── GserveConfig.java               # gserve 自身配置
│   └── domain/
│       └── EventBatchDTO.java              # 上报数据 DTO
└── src/main/resources/
    ├── bootstrap.yml                       # Nacos 配置
    └── application.yml                     # 本地配置
```

**system 模块新增（查询分析）**：

```
ruoyi-system/src/main/java/com/ruoyi/system/
└── controller/game/analytics/
    └── AnalyticsQueryController.java   # 统计分析查询接口
└── service/game/analytics/
    └── AnalyticsQueryService.java      # 查询服务，连接 ClickHouse 读取聚合数据
└── config/
    └── ClickHouseQueryConfig.java      # 查询用 ClickHouse 连接配置（复用路由）
```

### 7.2 SecureKey 认证架构

游戏服上报数据**不再使用 JWT 登录认证**，而是采用行业通用的 **SecureKey 签名认证**。认证在 Gateway 层完成，gserve 模块无需关心认证逻辑。

#### 7.2.1 整体认证流程

```
游戏服务器                        Gateway                             gserve 服务
    │                               │                                    │
    │ POST /gserve/etl/receive     │                                    │
    │ Headers:                      │                                    │
    │   X-App-Id:     {app_id}     │                                    │
    │   X-Timestamp:  1680000000   │                                    │
    │   X-Signature:  md5/hex...   │                                    │
    │   X-Sign-Type:  md5/sha256  │                                    │
    │ (Body: JSON 批量数据)         │                                    │
    │                               │                                    │
    │── ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ →  │                                    │
    │                               │  ① 按 X-App-Id 查找渠道 SecureKey │
    │                               │  ② 拼接签名原文，验证 X-Signature │
    │                               │  ③ 签名验证通过 → 转发 gserve    │
    │                               │  ④ 签名验证失败 → 返回 403       │
    │                               │── ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ →   │
    │                               │                                    │
    │←── 200 OK / 403 Forbidden ──│←── 200 OK / 处理结果 ──────────── │
```

#### 7.2.2 Gateway 扩展 — GserveAuthFilter

在 `ruoyi-gateway` 中新增 `GserveAuthFilter`（全局过滤器，order -100），专门处理 `/gserve/**` 路径的 SecureKey 认证：

| 配置项 | 值 |
|-------|-----|
| 过滤器类名 | `GserveAuthFilter` |
| 拦截路径 | `/gserve/**` |
| 过滤器顺序 | `-100`（在 AuthFilter order -200 之后，但仅处理 gserve 路径） |
| 认证方式 | SecureKey 签名验证 |
| 白名单 | 无（所有 gserve 请求均需签名） |

```java
@Component
@Order(-100)
public class GserveAuthFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (!path.startsWith("/gserve/")) {
            return chain.filter(exchange);  // 非 gserve 路径跳过
        }

        // 1. 从请求头提取认证信息
        String appId = exchange.getRequest().getHeaders().getFirst("X-App-Id");
        String timestamp = exchange.getRequest().getHeaders().getFirst("X-Timestamp");
        String signature = exchange.getRequest().getHeaders().getFirst("X-Signature");
        String signType = exchange.getRequest().getHeaders().getFirst("X-Sign-Type");

        // 2. 校验必填头
        if (appId == null || timestamp == null || signature == null) {
            return unauthorized(exchange, "缺少签名参数");
        }

        // 3. 校验时间戳是否在允许窗口内（±5 分钟防重放）
        long ts = Long.parseLong(timestamp);
        if (Math.abs(System.currentTimeMillis() / 1000 - ts) > 300) {
            return unauthorized(exchange, "时间戳已过期");
        }

        // 4. 根据 appId 查找渠道 SecureKey（从 Redis 或远程调用 auth 服务）
        return secureKeyService.getSecureKey(appId)
            .flatMap(secureKey -> {
                // 5. 验证签名
                String signContent = buildSignContent(exchange, timestamp);
                String expectedSign = sign(signContent, secureKey, signType);
                if (!expectedSign.equalsIgnoreCase(signature)) {
                    return unauthorized(exchange, "签名验证失败");
                }
                // 6. 将 appId/channelId 注入请求头，传递到下游
                ServerWebExchange mutated = mutateHeaders(exchange, appId);
                return chain.filter(mutated);
            })
            .switchIfEmpty(unauthorized(exchange, "无效的 AppId"));
    }
}
```

#### 7.2.3 Auth 模块扩展 — SecureKey 管理

在 `ruoyi-auth` 中新增 SecureKey 的生成和管理能力：

| 操作 | API 路径 | 说明 |
|------|---------|------|
| 生成 SecureKey | `POST /auth/channel/{channelId}/securekey` | 为指定渠道生成新的 SecureKey |
| 重置 SecureKey | `PUT /auth/channel/{channelId}/securekey` | 重置（旧 Key 立即失效） |
| 查询 SecureKey | `GET /auth/channel/{channelId}/securekey` | 获取渠道 SecureKey 信息 |
| 验证 SecureKey | `POST /auth/securekey/verify` | 供 Gateway 调用验证签名 |

**SecureKey 生成规则**：

```java
public String generateSecureKey() {
    // 128 位随机数，hex 编码为 32 位字符串
    byte[] key = new byte[16];
    SecureRandom secureRandom = new SecureRandom();
    secureRandom.nextBytes(key);
    return bytesToHex(key);
}
```

**存储**：SecureKey 的哈希值存储在 `game_channel` 表的 `secure_key_hash` 字段中，原始 Key 仅在生成时展示一次，后续不可查看（只可重置）。

```
原始 SecureKey（展示一次）：a1b2c3d4e5f6071829abcdef01234567
                 ↓ SHA-256
DB 存储（secure_key_hash）：e3b0c44298fc1c149afbf4c8996fb924...
```

### 7.3 渠道 SecureKey 配置

#### 7.3.1 数据库扩展

在现有 `game_channel` 表中新增字段：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| `secure_key_hash` | `varchar(128)` | SecureKey 的 SHA-256 哈希值 |
| `secure_key_salt` | `varchar(64)` | 生成签名时使用的盐值 |
| `secure_key_updated_at` | `datetime` | 最近一次 SecureKey 更新时间 |
| `secure_key_version` | `int(11)` | Key 版本号（轮换时递增） |

#### 7.3.2 后台管理页面

在现有**渠道管理页面**中增加 SecureKey 操作区：

```
┌──────────────────────────────────────────────┐
│  渠道管理 > 编辑渠道 > SecureKey 管理           │
│                                              │
│  渠道名称：华为应用市场                          │
│  App ID：huawei_app_001                      │
│                                              │
│  ┌────────────────────────────────────────┐  │
│  │ SecureKey 状态：已启用                    │  │
│  │ 上次更新：2026-05-01 14:30:00           │  │
│  │ Key 版本：v2                            │  │
│  │                                          │  │
│  │  [生成 SecureKey]  [重置 SecureKey]      │  │
│  └────────────────────────────────────────┘  │
│                                              │
│  * 生成成功后 SecureKey 仅展示一次，请妥善保管   │
│  * 重置后旧 Key 立即失效，游戏服需更新配置       │
└──────────────────────────────────────────────┘
```

**生成流程**：
1. 管理员点击「生成 SecureKey」
2. 后端生成 32 位 hex 随机串 + 盐值
3. 返回原始 SecureKey（仅本次请求展示）
4. 后端存储 `SHA-256(secureKey + salt)` 到 `secure_key_hash`
5. 前端提示：请立即保存 SecureKey，关闭后将无法再次查看

### 7.4 签名算法

#### 7.4.1 签名规则（推荐 HMAC-SHA256）

```
签名原文 = HTTP_METHOD + "\n" +
          URI_PATH + "\n" +
          X-Timestamp + "\n" +
          Body_MD5

Body_MD5 = MD5(requestBody)

Signature = HMAC-SHA256(签名原文, SecureKey)
```

**请求示例**：

```
POST /gserve/etl/receive
X-App-Id: huawei_app_001
X-Timestamp: 1680000000
X-Sign-Type: hmac-sha256
X-Signature: 9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08

Body: [{"event_type":"login","role_id":1001,...}]
```

#### 7.4.2 验签过程（Gateway 中实现）

```java
boolean verifySignature(String appId, String timestamp, String signType,
                        String signature, String method, String path, byte[] body) {

    // 1. 从 Redis/缓存获取渠道 SecureKey
    String secureKey = getSecureKey(appId);  // 原始 Key，从安全存储获取

    // 2. 计算 Body MD5
    String bodyMd5 = DigestUtils.md5DigestAsHex(body);

    // 3. 拼接签名原文
    String signContent = method + "\n" + path + "\n" + timestamp + "\n" + bodyMd5;

    // 4. 计算预期签名
    String expected;
    if ("hmac-sha256".equals(signType)) {
        SecretKeySpec keySpec = new SecretKeySpec(secureKey.getBytes(), "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(keySpec);
        expected = bytesToHex(mac.doFinal(signContent.getBytes()));
    } else {
        // 默认 MD5 方式（兼容）
        expected = DigestUtils.md5DigestAsHex((signContent + secureKey).getBytes());
    }

    // 5. 对比签名（恒定时间比较防时序攻击）
    return MessageDigest.isEqual(expected.getBytes(), signature.getBytes());
}
```

### 7.5 HTTP 数据上传流程

```
游戏服务器                          Gateway                          gserve 服务
    │                                │                                │
    │── 1. 组装 JSON 批量数据 ──→   │                                │
    │    body = [{event1},{event2}]  │                                │
    │                                │                                │
    │── 2. 计算签名 ────────────→   │                                │
    │    signContent = method + \n  │                                │
    │                  + path + \n  │                                │
    │                  + ts + \n    │                                │
    │                  + MD5(body)  │                                │
    │    signature = HMAC-SHA256    │                                │
    │                (signContent,  │                                │
    │                 secureKey)    │                                │
    │                                │                                │
    │── 3. HTTP POST ───────────→  │                                │
    │    Headers:                   │  4. 验证签名                     │
    │    X-App-Id, X-Timestamp,    │  5. 查找 SecureKey              │
    │    X-Signature, X-Sign-Type  │  6. 计算并对比签名               │
    │                                │  7. 注入 appId 到请求头        │
    │                                │── 8. 转发 ────────────────→  │
    │                                │                                │
    │                                │                                │  9. 校验数据格式
    │                                │                                │ 10. 写入 ClickHouse
    │                                │                                │     (批量 1000 条)
    │                                │                                │
    │←── 11. 200 OK / 400 / 500 ──│←── 响应 ──────────────────── │
```

### 7.6 数据校验与错误处理

**gserve 模块**在写入前对数据做以下校验：

| 校验项 | 规则 |
|--------|------|
| 必填字段 | 各表对应的必填字段不能为空 |
| 时间范围 | `event_time` 不能超过当前时间 ±24 小时 |
| 字段类型 | 数值字段不能包含非数字字符 |
| 批量大小 | 单次提交不超过 5000 条 |

**错误处理机制**：

```yaml
game:
  analytics:
    etl:
      error-log:
        enabled: true
        max-retention-days: 30
      retry:
        max-retries: 3
        retry-interval-ms: 1000
      dead-letter:
        enabled: true
        datasource: mysql     # 死信存 MySQL，支持人工补偿
```

校验不通过的数据写入 MySQL 死信表 `game_etl_error_log`：

```sql
CREATE TABLE game_etl_error_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    app_id VARCHAR(128) NOT NULL,
    raw_data JSON NOT NULL,
    error_msg VARCHAR(1024) NOT NULL,
    event_type VARCHAR(64) DEFAULT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    status TINYINT DEFAULT 0 COMMENT '0=待处理, 1=已重试, 2=已忽略'
);

---

## 8. 多项目架构与数据源路由

### 8.1 完整部署架构

```
                            ┌─────────────────────┐
                            │  Nacos 配置中心       │
                            │  :8848               │
                            └────────┬────────────┘
                                     │ 配置下发
         ┌───────────────────────────┼──────────────────────────────┐
         │                           │                              │
    ┌────┴─────┐              ┌──────┴──────┐              ┌───────┴──────┐
    │ ruoyi-system             │ ruoyi-gserve│              │ ruoyi-gateway│
    │ (后台管理)               │ (ETL / 查询) │              │ (网关)        │
    │ :9201                    │ :9400       │              │ :8080        │
    └────┬─────┘              └──────┬──────┘              └──────┬───────┘
         │                           │                            │
         │         ┌─────────────────┼─────────────────────┐      │
         │         │                 │                      │      │
    ┌────┴─────┐  ┌┴───────────┐  ┌─┴───────────┐          │  ┌───┴────┐
    │ MySQL    │  │ ClickHouse │  │ ClickHouse  │  ...      │  │ Redis  │
    │ (业务DB) │  │ (项目 A)   │  │ (项目 B)    │          │  │        │
    └──────────┘  └────────────┘  └─────────────┘          │  └────────┘
                                                           │
                                                      ┌────┴────────┐
                                                      │ 游戏服 HTTP  │
                                                      │ (SecureKey) │
                                                      └─────────────┘
```

### 8.2 请求路由说明

| 路径 | 网关路由目标 | 认证方式 | 说明 |
|------|-------------|---------|------|
| `/system/**` | `ruoyi-system` | JWT 登录认证 | 后台管理端 API（含统计查询） |
| `/gserve/etl/**` | `ruoyi-gserve` | SecureKey 签名 | 游戏服数据上报（纯 ETL） |

### 8.3 注解 + AOP 动态数据源切换

在 `gserve` 模块和 `system` 模块中，借鉴现有 `@GameDb` / `@LogDb` 注解和 `GameRoutingDataSource` 的 AOP 模式，新增 `@ClickHouseDb` 注解：

```java
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ClickHouseDb {
    /** 项目 ID，为空则从请求上下文自动获取 */
    long projectId() default 0;
}
```

**路由流程（gserve / system 共用）**：

```
gserve 接收到转发请求 或  system 发起查询请求
    │
    ├── 从请求头提取 project_id (或 app_id → 映射 project_id)
    │
    ├── ClickHouseDataSourceAspect 拦截 @ClickHouseDb 注解
    │
    ├── 根据 project_id 查询 game_project 表中的 clickhouse_config
    │     └── 使用 Redis 缓存查询结果（TTL 300 秒），避免频繁读 DB
    │
    ├── ClickHouseRoutingDataSource.lookup(projectId)
    │     └── 如果已创建 → 直接返回缓存的数据源
    │     └── 如果未创建 → 解析 clickhouse_config JSON → 创建数据源并缓存
    │
    └── 执行 ClickHouse 写入 / 查询
```

### 8.4 项目表扩展 — clickhouse_config 字段

在现有 `game_project` 表中新增 `clickhouse_config` JSON 字段，用于管理该项目的 ClickHouse 连接信息：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| `clickhouse_config` | `text` | **ClickHouse 连接配置 JSON**（结构见下方） |

**clickhouse_config JSON 结构**：

```json
{
  "host": "192.168.1.100",
  "port": 8123,
  "database": "game_analytics_mhxx",
  "username": "default",
  "password": "",
  "minPoolSize": 2,
  "maxPoolSize": 8
}
```

### 8.5 项目管理页面扩展

在现有项目管理页面的配置区域中，增加 **ClickHouse 配置** 编辑区：

```
┌──────────────────────────────────────────────────┐
│  项目管理 > 编辑项目 > ClickHouse 配置              │
│                                                    │
│  项目名称：梦幻修仙                                │
│                                                    │
│  ┌──────────────────────────────────────────┐     │
│  │  ClickHouse 数据源                        │     │
│  │                                          │     │
│  │  主机地址：  [192.168.1.100           ]  │     │
│  │  端口号：    [8123                    ]  │     │
│  │  数据库名：  [game_analytics_mhxx     ]  │     │
│  │  用户名：    [default                 ]  │     │
│  │  密码：      [********                ]  │     │
│  │  最小连接数： [2                       ]  │     │
│  │  最大连接数： [8                       ]  │     │
│  │                                          │     │
│  │  [测试连接]  [保存配置]                   │     │
│  └──────────────────────────────────────────┘     │
│                                                    │
└──────────────────────────────────────────────────────┘
```

**测试连接功能**：保存前可测试 ClickHouse 连接是否可达，避免配置错误导致写入失败。

### 8.6 数据隔离说明

| 隔离层次 | 方式 | 说明 |
|---------|------|------|
| 项目间 | 物理隔离（不同 ClickHouse 实例/数据库） | 不同项目的日志数据互不可见，通过各项目自身的 clickhouse_config 连接 |
| 服间 | 逻辑隔离（server_id 字段） | 同一项目内各服通过 server_id 区分 |

---

## 9. Nacos 配置管理

所有配置统一在 Nacos 中管理，与现有项目的配置风格一致。

### 9.1 gserve 模块配置

> ClickHouse 项目级数据源配置**不在 Nacos 中管理**，而是在 `game_project` 表的 `clickhouse_config` 字段中配置（见 8.4 节），通过项目管理页面维护。

**Data ID**: `ruoyi-gserve.yml`
**Group**: `DEFAULT_GROUP`

```yaml
server:
  port: 9400

spring:
  application:
    name: ruoyi-gserve
  clickhouse:
    enabled: true
    # 默认连接池兜底配置（各项目的具体连接信息从 game_project.clickhouse_config 读取）
    pool:
      connection-timeout: 30000
      idle-timeout: 600000

# gserve 自身配置
game:
  analytics:
    etl:
      max-batch-size: 5000      # 单次最大接收条数
      default-batch-size: 1000  # ClickHouse 写入批次大小
      thread-pool: 4            # 写入线程数
      retry:
        max-retries: 3
        retry-interval-ms: 1000
      error-log:
        enabled: true
        max-retention-days: 30
```

### 9.2 Gateway SecureKey 认证配置

在 Gateway 的配置文件中增加 SecureKey 认证相关配置：

```yaml
# Data ID: ruoyi-gateway-resource.yml (扩展)
spring:
  cloud:
    gateway:
      routes:
        # gserve 路由 - 数据上报（SecureKey 认证）
        - id: ruoyi-gserve-etl
          uri: lb://ruoyi-gserve
          predicates:
            - Path=/gserve/etl/**
          filters:
            - StripPrefix=1
        # gserve 路由 - 管理端查询（JWT 认证走已有 AuthFilter）
        - id: ruoyi-gserve-query
          uri: lb://ruoyi-gserve
          predicates:
            - Path=/gserve/query/**
          filters:
            - StripPrefix=1

# SecureKey 认证配置
game:
  gateway:
    securekey:
      enabled: true
      # 签名验证时间窗口（秒），防重放攻击
      timestamp-window: 300
      # 支持的签名类型
      sign-types: md5, hmac-sha256
      # SecureKey 缓存时间（秒）
      key-cache-ttl: 3600
      # auth 服务地址（用于获取 SecureKey）
      auth-service: ruoyi-auth
```

### 9.3 渠道表 SecureKey 字段

在现有 `game_channel` 表基础上新增的 Nacos 配置管理条目：

```yaml
# Data ID: ruoyi-system-dict.yml (扩展)
game:
  channel:
    securekey:
      enabled: true
      # 生成算法
      algorithm: SHA256PRNG
      # 密钥长度（字节）
      key-length: 16
      # hex 编码后的长度（32 字符）
      output-length: 32
      # 是否允许管理员查看 Key（false = 仅生成时展示一次）
      allow-view: false
```

---

## 10. 物化视图建议（后续迭代）

| 视图名称 | 聚合粒度 | 用途 |
|---------|---------|------|
| `game_agg_dau_daily` | server + dt | 日活跃用户数、新增回流 |
| `game_agg_revenue_daily` | server + dt | 日流水、付费人数、ARPU |
| `game_agg_retention_daily` | create_dt + gap_days | 留存率预计算 |
| `game_agg_level_distribution` | server + dt + level | 等级分布快照 |

---

## 11. 已确认问题

| # | 问题 | 结论 |
|---|------|------|
| 1 | event_type 编码管理方式 | 服务端自由上报，后台提供事件类型配置页面，定义 `n1~n10` / `s1~s5` 的参数语义和枚举值解析，用于前端查询展示 |
| 2 | Log DB 数据同步方案 | 先实现 HTTP 直插策略（游戏服→Gateway→gserve→ClickHouse），后续可扩展 Canal+Kafka 或批量导入 |
| 3 | ClickHouse 部署方式 | 独立部署 |
| 4 | ClickHouse 数据源配置方式 | **不在 Nacos 中管理**，在项目管理页面配置，存储于 `game_project.clickhouse_config` JSON 字段。gserve 和 system 模块均从该字段读取连接信息 |
| 5 | 分服还是合服 | 按项目分 ClickHouse 实例（每个项目通过 clickhouse_config 连接独立数据源），项目内 server_id 做逻辑隔离。注解 + AOP 动态切换数据源 |
| 6 | 历史数据导入 | 暂不导入历史数据，从需求确定后的时间点开始采集 |
| 7 | 新增 VIP/战力字段 | 除 `game_event_role_event` 外所有表增加 `vip`（UInt16）和 `fight`（UInt64），玩家事件时刻快照值，服务端随事件上报 |
| 8 | ETL 独立模块 | 新增 `ruoyi-modules/ruoyi-gserve` 模块，**只做数据接收与写入**，不做统计查询 |
| 9 | 统计查询在哪实现 | 在 `ruoyi-system` 模块的 `AnalyticsQueryController` 中实现，gserve 不包含查询功能 |
| 10 | 上报认证方式 | 不使用 JWT 登录，改用 SecureKey 签名认证（HMAC-SHA256 / MD5） |
| 11 | Gateway 扩展 | 新增 `GserveAuthFilter`，拦截 `/gserve/**` 路径做签名验证 |
| 12 | Auth 模块扩展 | 支持渠道 SecureKey 的生成、重置、验证，Key 仅生成时展示一次 |
| 13 | 渠道配置扩展 | `game_channel` 表新增 `secure_key_hash`、`secure_key_salt` 等字段 |
