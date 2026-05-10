# 游戏数据分析模块 — PRD 需求文档

> 版本：v1.1
> 日期：2026-05-08
> 基于：ClickHouse 6 张事件表 + 通用 ServerSelector 组件

---

## 1. 概述

### 1.1 目标

将当前简易的「数据分析仪表盘」重构为**5 个独立分析页面** + **1 个每日总览页**，提供接近商业化游戏运营平台的数据分析能力。

### 1.2 页面清单（共 8 页）

| # | 页面 | 路由 | 说明 |
|---|------|------|------|
| 1 | 每日总览 | `analytics/overview` | **已完成**。核心日报指标，按日期/服务器两 Tab 展示。汇总行常显，支持展开。含 4 趋势图。 |
| 2 | 留存分析 | `analytics/retention` | 按创角日期留存，多列展示 1~7 日 + 14/30 日 |
| 3 | LTV 分析 | `analytics/ltv` | 按创角日期 LTV，多列展示 LTV1~7 + LTV14/30 |
| 4 | 充值分析 | `analytics/recharge` | 按服务器/日期维度，日流水/付费率/ARPU/ARPPU |
| 5 | 等级分析 | `analytics/level` | 等级分布 + 升级速度 + 各等级流失 |
| 6 | 道具经济 | `analytics/item` | 道具产出/消耗明细 |
| 7 | 玩家行为日志 | `analytics/eventLog` | 已实现，事件语义解析展示 |

### 1.3 公共交互模式

所有页面共用：
- **ServerSelector** 四级选服组件（项目→渠道→分区→服务器）
- **顶部操作栏**：ServerSelector 内嵌日期范围选择 + 查询/重置按钮
- **底部汇总行**：表格最后一行显示合计值

---

## 2. 页面详设

### 2.1 每日总览（`analytics/overview`）

这是最核心的页面，整合多个数据维度的核心指标一览。

**筛选项**：ServerSelector（含日期范围）

**表格列设计**：

| 列 | 字段 | 数据源 | 计算逻辑 |
|----|------|--------|---------|
| **服务器** | server_id | — | 分组维度 |
| **日期** | dt | — | 分组维度 |
| 注册角色数 | reg_roles | chara_creation | `count(DISTINCT role_id)` |
| 登录角色数 | login_roles | role_login(login) | `count(DISTINCT role_id)` |
| 付费角色数 | pay_roles | recharge(success) | `count(DISTINCT role_id)` |
| 付费次数 | pay_times | recharge(success) | `count()` |
| 人均付费次数 | avg_pay_times | — | `pay_times / pay_roles` |
| 付费率 | pay_rate | — | `pay_roles / login_roles` |
| 付费金额 | pay_amount | recharge | `sum(amount)` |
| 付费 ARPU | pay_arpu | — | `pay_amount / login_roles` |
| 付费 ARPPU | pay_arppu | — | `pay_amount / pay_roles` |
| 登录 ARPU | login_arpu | — | `pay_amount / login_roles`（同付费ARPU） |

**新增玩家子表**（仅当天创角玩家）：

| 列 | 计算逻辑 |
|----|---------|
| 新增注册角色数 | chara_creation WHERE dt = 当天 |
| 新增登录角色数 | 新增 + 当天有登录 |
| 新增付费角色数 | 新增 + 当天有付费 |
| 新增付费次数 | 新增 + 当天付费次数 |
| 新增付费率 | 新增付费角色数 / 新增登录角色数 |
| 新增付费金额 | 新增 + 当天付费金额 |
| 新增付费 ARPU | 新增付费金额 / 新增登录角色数 |
| 新增登录 ARPU | 新增付费金额 / 新增登录角色数 |

**非新增玩家子表**（非当天创角的回流玩家）：

| 列 | 计算逻辑 |
|----|---------|
| 非新增登录角色数 | 登录角色 - 新增登录角色 |
| 非新增付费角色数 | 付费角色 - 新增付费角色 |
| 非新增付费次数 | 总付费次数 - 新增付费次数 |
| 非新增付费率 | 非新增付费角色 / 非新增登录角色 |
| 非新增付费金额 | 总付费金额 - 新增付费金额 |
| 非新增付费 ARPU | 非新增付费金额 / 非新增登录角色 |

**留存列**（按 dt 的创角队列回溯）：

| 列 | 计算逻辑 |
|----|---------|
| 1留 (Day1) | dt 当天创角 + Day2 登录 / dt 当天创角 |
| 2留 (Day2) | dt 当天创角 + Day3 登录 / dt 当天创角 |
| ... | ... |
| 7留 (Day7) | dt 当天创角 + Day8 登录 / dt 当天创角 |
| 14留 (Day14) | dt 当天创角 + Day15 登录 / dt 当天创角 |
| 30留 (Day30) | dt 当天创角 + Day31 登录 / dt 当天创角 |

**LTV 列**：

| 列 | 计算逻辑 |
|----|---------|
| LTV1 | 创角当日 + 次日累计付费 / 创角数 |
| LTV2 | 创角当日 + 前2日累计付费 / 创角数 |
| ... | ... |
| LTV7 / LTV14 / LTV30 | 同上递推 |

**表格底部汇总行**：所有行数值相加或加权平均。

**交互**：
- 首次加载无默认数据，需点查询
- 服务器列支持多服展开，每个 server_id 一行
- 分区/渠道按选择层级聚合到对应的 server_id 维度

---

### 2.2 留存分析（`analytics/retention`）

独立留存分析页，聚焦于创角用户的长期留存趋势。

**筛选项**：ServerSelector（含日期范围，默认最近 14 天创角）

**表格列**：

| 列 | 说明 |
|----|------|
| 创角日期 | dt（来自 chara_creation） |
| 服务器 | server_id |
| 创角用户数 | base count |
| 1留 | Day1 retention |
| 2留 | Day2 retention |
| 3留 | Day3 retention |
| 4留 | Day4 retention |
| 5留 | Day5 retention |
| 6留 | Day6 retention |
| 7留 | Day7 retention |
| 14留 | Day14 retention |
| 30留 | Day30 retention |

**底部汇总**：每个留存列的加权平均值（以创角用户数为权重）。

---

### 2.3 LTV 分析（`analytics/ltv`）

独立 LTV 分析页。

**筛选项**：ServerSelector（含日期范围，默认最近 14 天创角）

**表格列**：

| 列 | 说明 |
|----|------|
| 创角日期 | dt |
| 服务器 | server_id |
| 创角用户数 | base count |
| LTV1 | 人均前 1 日累计付费 |
| LTV2 | 人均前 2 日累计付费 |
| LTV3 | 人均前 3 日累计付费 |
| LTV4 | 人均前 4 日累计付费 |
| LTV5 | 人均前 5 日累计付费 |
| LTV6 | 人均前 6 日累计付费 |
| LTV7 | 人均前 7 日累计付费 |
| LTV14 | 人均前 14 日累计付费 |
| LTV30 | 人均前 30 日累计付费 |

**底部汇总**：加权平均（权重=创角用户数）。

**额外**：可切换按「渠道」或「分区」聚合维度。

---

### 2.4 充值分析（`analytics/recharge`）

日粒度付费数据。

**筛选项**：ServerSelector（含日期范围）

**表格列**：

| 列 | 说明 |
|----|------|
| 服务器 | server_id |
| 日期 | dt |
| 付费人数 | `count(DISTINCT role_id)` |
| 付费次数 | `count()` |
| 人均付费次数 | `付费次数 / 付费人数` |
| 付费率 | `付费人数 / 登录人数` |
| 付费金额 | `sum(amount)` |
| ARPU | `付费金额 / 登录人数` |
| ARPPU | `付费金额 / 付费人数` |
| 首充人数 | `is_first_recharge = 1` |
| 首充金额 | 首充用户的 amount 总和 |

**底部汇总**：所有数值列求和，比率列加权平均。

---

### 2.5 等级分析（`analytics/level`）

**筛选项**：ServerSelector（含日期，默认今天）

**上半部分 — 等级分布**：

| 列 | 说明 |
|----|------|
| 等级 | new_level |
| 角色数 | `count(DISTINCT role_id)` |

**下半部分 — 升级速度**：

| 列 | 说明 |
|----|------|
| 起始等级-目标等级 | fromLevel - toLevel |
| 平均耗时（小时） | `avg(cost_seconds) / 3600` |
| 角色数 | count |

**底部汇总**：总角色数。

---

### 2.6 道具经济（`analytics/item`）

**筛选项**：ServerSelector + 道具搜索框

**表格列**：

| 列 | 说明 |
|----|------|
| 服务器 | server_id |
| 日期 | dt |
| 道具 ID | item_id |
| 道具名称 | item_name |
| 产出数量 | `sumIf(item_num, item_num > 0)` |
| 消耗数量 | `sumIf(abs(item_num), item_num < 0)` |
| 涉及玩家数 | `count(DISTINCT role_id)` |

**底部汇总**：产出/消耗合计。

---

## 3. 后端接口设计

### 3.1 每日总览接口

```
GET /analytics/overview
参数: projectId, serverIds, beginDate, endDate
返回: List<Map<String, Object>>
```

**核心 SQL 策略**：一次性查出三个维度的数据（total / new / non-new），按 `server_id, dt` 分组。

### 3.2 留存 / LTV / 充值 / 等级 / 道具

各页面独立接口，参数和返回格式与现有模式一致。

### 3.3 汇总行计算

在前端完成：分别 sum 数值列、weighted average 比率列。

---

## 4. 前端实现要点

### 4.1 路由

| 路由 | 组件 |
|------|------|
| `analytics/overview` | `views/game/analytics/overview.vue` |
| `analytics/retention` | `views/game/analytics/retention.vue` |
| `analytics/ltv` | `views/game/analytics/ltv.vue` |
| `analytics/recharge` | `views/game/analytics/recharge.vue` |
| `analytics/level` | `views/game/analytics/level.vue` |
| `analytics/item` | `views/game/analytics/item.vue` |
| `analytics/eventLog` | `views/game/analytics/eventLog.vue`（已有） |

### 4.2 组件复用

所有页面的 ServerSelector + 查询按钮封装为一个 `<AnalyticsFilter>` 组件，避免每个页面重复写。

### 4.3 底部汇总行

Element UI 的 `el-table` 支持 `show-summary` + `summary-method`，自定义汇总逻辑。

---

## 5. 菜单调整

| 菜单 ID | 名称 | 路由 | 顺序 |
|---------|------|------|------|
| 2700 | 数据分析 | — | 父级 |
| 2701 | 每日总览 | `analytics/overview` | 1 |
| 2702 | 留存分析 | `analytics/retention` | 2 |
| 2703 | LTV分析 | `analytics/ltv` | 3 |
| 2704 | 充值分析 | `analytics/recharge` | 4 |
| 2705 | 等级分析 | `analytics/level` | 5 |
| 2706 | 道具经济 | `analytics/item` | 6 |
| 2710 | 玩家行为日志 | `analytics/eventLog` | 7 |
| 2720 | 事件类型配置 | `analytics/eventType` | 8 |

---

## 6. 已确认问题

| # | 问题 | 结论 |
|---|------|------|
| 1 | 留存/LTV 回溯基准 | **当天创角日**。即留存表显示"X月X日创角的用户到Day N的留存率" |
| 2 | 是否需要 Excel 导出 | **需要**。每个页面均支持导出当前查询结果为 Excel |
| 3 | "登录树"展示形式 | **表格为主 + 折线图为辅**。表格展示数值，顶部嵌入 ECharts 折线图展示 DAU 趋势 |
| 4 | 汇总行显示 | **常显**。表格 `show-summary` 始终显示，不收起 |

### 6.1 留存/LTV 页面切换维度

留存和 LTV 页面顶部增加 Tab 切换：

| Tab | 维度 | 说明 |
|-----|------|------|
| 按服务器 | server_id | 每个服务器一行，跨日期汇总 |
| 按渠道 | channel_id | 每个渠道一行，按所选渠道聚合 |
| 按日期 | dt | 每天一行，所有已选服务器/渠道合并
