# 游戏内部资源申请模块 PRD

## 1. 概述

为运营、QA、客服等内部人员提供统一的游戏资源申请、审批、发放流程。支持申请游戏内道具、货币、等级调整等资源，并自动通过服务端推送到目标游戏服务器。

## 2. 功能需求

### 2.1 核心流程

```
申请（申请人）→ 审批（审批人）→ 发放（系统推送到游戏服）
```

### 2.2 申请单核心字段

| 字段 | 类型 | 说明 |
|------|------|------|
| 标题 | string(128) | 申请标题 |
| 申请类型 | enum | 道具发放 / 货币调整 / 等级调整 / 创建角色 / 自定义 |
| 项目 | ref | 目标项目（ServerSelector） |
| 服务器 | ref | 目标服务器（ServerSelector多选） |
| 玩家ID | string | 目标角色ID（playerId），支持多行 |
| 资源内容 | JSON | 具体资源列表 `[{type, id, name, count}]` |
| 申请理由 | text | 事由说明 |
| 紧急程度 | enum | 普通 / 紧急 |
| 期望完成时间 | datetime | 最晚需要的时间 |
| 状态 | enum | 待审批 / 审批通过 / 已发放 / 发放失败 / 驳回 / 已撤回 |
| 审批人 | string | 指定的审批人 |
| 审批意见 | text | 驳回或审批备注 |

### 2.3 资源内容（ResourceItem）

```
ResourceItem {
    type: enum       # 资源类型：item(道具), currency(货币), level(等级), character(创建角色)
    itemId: Long     # 道具ID（type=item时）
    itemName: string # 道具名称（回显）
    count: int      # 数量
    remark: string  # 备注（如等级调整的目标等级）
}
```

### 2.4 审批工作流

```
[草稿] → 提交 → [待审批] ── 通过 → [待发放] → 系统推送 → [已发放]
                        │                         └── → [发放失败]
                        └── 驳回 → [已驳回]
```

- 申请提交后通知审批人（可在系统内通知栏显示）
- 审批通过后自动触发推送（复用 `IServerPushService`）
- 发放失败可手动重试

## 3. 页面设计

### 3.1 申请管理页

路径：`/gm/resource`

- 列表表格：申请单号、标题、申请类型、申请人、状态、创建时间
- 筛选：类型、状态、创建时间范围
- 操作：查看详情、新建申请、撤回（草稿/待审批状态）
- 新建弹窗/页面：表单调 ServerSelector + 玩家ID + 资源列表

### 3.2 审批管理页

路径：`/gm/resource/audit`

- 列表表格：待审批/已处理申请
- 操作：通过、驳回（填写原因）

### 3.3 新建申请表单

```
┌─────────────────────────────────────────┐
│ ──── 服务器选择（顶部） ────              │
│ [          ServerSelector              ] │
│ ──── ────                                │
│ 标题        [_________________________] │
│ 申请类型    道具发放 ○ 货币调整 ○ 等级调整 │
│ 玩家ID      [_________________________] │
│             提示：每行一个角色ID           │
│ ──── 资源内容 ────                        │
│ [道具快捷选择器]  [数量]  [备注]  [删除]    │
│ [添加资源行]                                │
│ 申请理由    [_________________________] │
│ 紧急程度    普通 ○ 紧急                    │
│ 审批人      [审批人下拉选择]               │
│ ──── ────                                │
│ [提交申请] [保存草稿]                      │
└─────────────────────────────────────────┘
```

### 3.4 审批详情页（只读 + 操作）

```
┌─ 申请信息 ──────────────────────────────┐
│ 标题：xxxx                                │
│ 申请人：xxx    申请时间：2026-05-26        │
│ 类型：道具发放   服务器：项目A / 1服,2服   │
│ 玩家ID：1001, 1002                       │
│ ── 资源内容 ──                            │
│ 一箱金币 x 2                              │
│ 钻石 x 1000                              │
│ 理由：给QA测试用                          │
│ 状态：待审批                              │
│ ───────────────────────────────────       │
│ [通过] [驳回]                              │
└─────────────────────────────────────────┘
```

## 4. 数据结构

### t_gm_resource_request（申请表）

| 字段 | 类型 | 说明 |
|------|------|------|
| request_id | BIGINT PK | 申请单ID |
| title | VARCHAR(256) | 申请标题 |
| request_type | TINYINT | 0=道具 1=货币 2=等级 3=创建角色 4=自定义 |
| project_id | BIGINT | 项目ID |
| server_ids | TEXT | 目标服务器ID列表 |
| player_ids | TEXT | 目标角色ID列表（逗号分隔） |
| resources | TEXT | 资源内容JSON |
| reason | TEXT | 申请理由 |
| urgency | TINYINT | 0=普通 1=紧急 |
| expected_time | DATETIME | 期望完成时间 |
| status | TINYINT | 0=待审批 1=审批通过 2=已发放 3=发放失败 4=驳回 5=已撤回 |
| applicant | VARCHAR(64) | 申请人 |
| approver | VARCHAR(64) | 审批人 |
| audit_remark | VARCHAR(256) | 审批意见 |
| failed_server_ids | TEXT | 发放失败的服务器ID |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

## 5. 技术实现

### 5.1 复用现有组件

| 组件 | 用途 |
|------|------|
| **ServerSelector** | 项目/服务器选择 |
| **ItemSelector** | 道具快捷选择（道具发放类型） |
| **RichEditor** | 自定义内容时富文本编辑 |
| **IServerPushService** | 通过后推送到游戏服务器 |

### 5.2 后端

- Controller: `GameResourceRequestController.java`
- Service: `IGameResourceRequestService` / `GameResourceRequestServiceImpl`
- Mapper: `GameResourceRequestMapper`（MyBatis 注解）
- Domain: `GameResourceRequest`
- 推送复用已封装的 `IServerPushService`

### 5.3 前端

- 管理页：`src/views/gm/resource/index.vue`
- 审核页：`src/views/gm/resource/audit.vue`  
- 表单页：`src/views/gm/resource/form.vue`
- API 文件：`src/api/gm/resource.js`

### 5.4 和服务端交互的推送字段

审批通过 → 推送到游戏服 `/resource` 接口，参数：

```json
{
  "requestId": 1,
  "playerIds": "1001,1002",
  "resources": "[{\"type\":\"item\",\"itemId\":27000012,\"itemName\":\"一箱金币\",\"count\":1}]",
  "serverId": 1,
  "sign": "md5签名"
}
```

## 6. 权限

| 权限标识 | 说明 |
|---------|------|
| `gm:resource:list` | 查看申请列表 |
| `gm:resource:add` | 新建申请 |
| `gm:resource:edit` | 编辑（草稿/驳回状态） |
| `gm:resource:remove` | 删除 |
| `gm:resource:audit` | 审批操作 |

## 7. 与 GM 邮件模块的差异

| 维度 | GM 邮件 | 资源申请 |
|------|---------|---------|
| 目标 | 全体玩家 | 指定角色（单人或多人） |
| 内容 | 文本+道具奖励 | 道具/货币/等级等资源 |
| 场景 | 运营活动/通知 | QA测试/内部使用 |
| 审批 | 有 | 有（相同模式） |
| 推送路径 | `/mail` | `/resource` |
