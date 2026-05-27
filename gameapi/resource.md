# 资源发放推送接口

## 概述

管理后台内部资源申请模块审批通过后，向游戏服务器推送资源发放请求，由游戏服负责给指定角色发放道具、货币、调整等级等。

## 请求

### 路径

```
POST {backendUrl}/resource
```

### 请求参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `requestId` | long | 是 | 申请单ID |
| `playerIds` | string | 是 | 目标角色ID列表（逗号分隔） |
| `resources` | string | 是 | 资源内容JSON |
| `serverId` | int | 是 | 当前推送的目标服务器ID |
| `sign` | string | 是 | MD5签名 |

### resources 格式

```jsonc
[
  {
    "type": "item",        // 资源类型：item=道具, currency=货币, level=等级, character=创建角色, custom=自定义
    "itemId": 27000012,    // 道具ID（type=item时）
    "itemName": "一箱金币", // 道具名称（回显用）
    "count": 1,            // 数量
    "remark": "99"         // 备注（type=level时为目标等级）
  }
]
```

### 请求示例

```json
{
  "requestId": 1,
  "playerIds": "1001,1002",
  "resources": "[{\"type\":\"item\",\"itemId\":27000012,\"itemName\":\"一箱金币\",\"count\":1}]",
  "serverId": 1,
  "sign": "a1b2c3d4e5f6..."
}
```

### 签名示例

假设参数为：
```
playerIds=1001,1002
requestId=1
resources=[{"type":"item","itemId":27000012,"itemName":"一箱金币","count":1}]
serverId=1
```

密钥为 `abc123`，则签名字符串为：
```
playerIds1001,1002requestId1resources[{"type":"item","itemId":27000012,"itemName":"一箱金币","count":1}]serverId1abc123
```

`sign = MD5(签名字符串)`

## 响应

| 状态码 | 说明 |
|--------|------|
| 200 | 资源发放请求接收成功 |
| 非200 | 推送失败（管理后台会记录并允许重推） |
