# ServerSelector 组件使用规范

## 1. 组件概述

`ServerSelector`（`src/components/game/ServerSelector.vue`）是项目统一的**项目→渠道→分区→服务器**四级联动选择组件。所有涉及游戏服务器选择的页面必须使用此组件。

## 2. 基础用法

```vue
<template>
  <ServerSelector v-model="serverSelection" :showDatePicker="false" />
</template>

<script>
import ServerSelector from "@/components/game/ServerSelector"

export default {
  components: { ServerSelector },
  data() {
    return {
      serverSelection: { projectId: null, channelIds: [], regionIds: [], serverIds: [] }
    }
  }
}
</script>
```

### Props

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `value` | Object | `{projectId: null, serverIds: []}` | v-model 绑定值 |
| `showDatePicker` | Boolean | `true` | 是否显示底部的日期选择器（分析页面用，表单页设置为 false） |

### v-model 输出格式

```jsonc
{
  projectId: 1,          // Long | null  选中项目ID
  channelIds: [1, 2],    // [Long]      选中渠道ID列表
  regionIds: [3, 4],     // [Long]      选中分区ID列表
  serverIds: [1, 2, 3]   // [Int]       选中服务器ID列表（server_id）
}
```

## 3. 存储字段

使用 ServerSelector 的表单需要在对应数据库表中存储以下字段：

| 字段 | 类型 | 说明 |
|------|------|------|
| `project_id` | BIGINT | 项目ID |
| `channel_ids` | TEXT | 逗号分隔的渠道ID列表 |
| `region_ids` | TEXT | 逗号分隔的分区ID列表 |
| `server_ids` | TEXT | 逗号分隔的服务器ID列表（server_id） |

## 4. 回填（编辑模式）

### 4.1 标准回填方式

编辑已有数据时，**必须**使用 `_forceRestore: true` 标志，触发 ServerSelector 重新加载并恢复选中状态：

```javascript
// 编辑时从 API 获取数据后回填
if (data.projectId) {
  this.serverSelection = {
    projectId: data.projectId,
    channelIds: data.channelIds ? data.channelIds.split(',').map(Number) : [],
    regionIds: data.regionIds ? data.regionIds.split(',').map(Number) : [],
    serverIds: data.serverIds ? data.serverIds.split(',').map(Number) : [],
    _forceRestore: true  // 强制触发重新加载（避免因 projectId 相同被跳过）
  }
}
```

> **注意**：如果 `channelIds`/`regionIds` 为空（如资源申请表未存储渠道/分区），ServerSelector 会自动全选所有渠道和分区来级联加载服务器，确保服务器勾选能正常恢复。无需外部额外处理。

### 4.2 保存时提取

```javascript
const data = { ...this.form }
data.projectId = this.serverSelection.projectId || undefined
data.serverIds = this.serverSelection.serverIds.length > 0
  ? this.serverSelection.serverIds.join(',') : undefined
data.channelIds = this.serverSelection.channelIds && this.serverSelection.channelIds.length > 0
  ? this.serverSelection.channelIds.join(',') : undefined
data.regionIds = this.serverSelection.regionIds && this.serverSelection.regionIds.length > 0
  ? this.serverSelection.regionIds.join(',') : undefined
```

## 5. 场景示例

### 5.1 GM 邮件（`views/gm/mail/form.vue`）

**模板**：
```vue
<ServerSelector v-model="serverSelection" :showDatePicker="false" />
```

**回填**（编辑时）：
```javascript
this.serverSelection = {
  projectId: data.projectId,
  channelIds: data.channelIds ? data.channelIds.split(',').map(Number) : [],
  regionIds: data.regionIds ? data.regionIds.split(',').map(Number) : [],
  serverIds: data.serverIds ? data.serverIds.split(',').map(Number) : [],
  _forceRestore: true
}
```

**保存**：
```javascript
data.projectId = this.serverSelection.projectId || undefined
data.serverIds = this.serverSelection.serverIds.length > 0
  ? this.serverSelection.serverIds.join(',') : undefined
data.channelIds = this.serverSelection.channelIds.length > 0
  ? this.serverSelection.channelIds.join(',') : undefined
data.regionIds = this.serverSelection.regionIds.length > 0
  ? this.serverSelection.regionIds.join(',') : undefined
```

**数据表**：`t_gm_mail`

### 5.2 资源申请（`views/gm/resource/form.vue`）

**模板**：
```vue
<ServerSelector v-model="serverSelection" :showDatePicker="false" />
```

**回填**（编辑时）：
```javascript
this.serverSelection = {
  projectId: d.projectId,
  channelIds: [],
  regionIds: [],
  serverIds: d.serverIds ? d.serverIds.split(',').map(Number) : [],
  _forceRestore: true
}
```

> 资源申请暂不存储渠道/分区，回填时传空数组即可。`_forceRestore` 确保 ServerSelector 重新加载。

**保存**：同上规则。

**数据表**：`t_gm_resource_request`

### 5.3 分析页面（带日期筛选）

```vue
<ServerSelector v-model="serverSelection" @query="onQuery" />
```

分析页面使用 `showDatePicker` 默认值 `true`（显示日期选择器），并监听 `@query` 事件触发查询。

## 6. 内部回填机制

### 6.1 value 监听器

ServerSelector 通过 `watch.value` 监听外部 v-model 变化：

1. `projectId` 为 null → 清空所有选中状态（应用于新建表单重置）
2. `_forceRestore: true` → 强制触发重新加载（即使 projectId 与当前相同）
3. 正常外部赋值 → 设置 `_restoring = true`，级联加载渠道→分区→服务器数据

### 6.2 级联加载流程

```
设置 projectId → 加载渠道列表 → channels watch 触发
  → 自动选中渠道（从 value.channelIds）→ 加载分区列表
  → regions watch 触发 → 自动选中分区（从 value.regionIds）
  → 加载服务器列表 → loadServers 匹配 value.serverIds 勾选
```

### 6.3 重置

在父组件的 `resetFormData()` 中：

```javascript
this.serverSelection = { projectId: null, channelIds: [], regionIds: [], serverIds: [] }
```

`projectId` 为 null 时 ServerSelector 自动清空内部状态。

## 7. 注意事项

1. **必须存 channelIds/regionIds**：虽然有些页面当前不需要渠道/分区数据，但不存会导致回填时无法正确恢复服务器列表（渠道/分区未选中时服务器区块不可见）
2. **`_forceRestore` 不可遗漏**：编辑回填时必须加此标志，否则 ServerSelector 可能跳过更新
3. **`showDatePicker`**：表单类页面传 `false`，分析类页面用默认值 `true`
4. **数据表字段类型**：全部使用 `TEXT` 类型存储逗号分隔的 ID 字符串
