# 订单接口

## 1. 预创建订单

玩家在游戏客户端发起充值，SDK 拉起支付前调用此接口创建订单。

### 请求

```
POST /bridge/order/preCreate
Content-Type: application/json
```

### 请求参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `playerId` | long | 是 | 游戏角色ID |
| `identityId` | string | 是 | 平台唯一标识（来自 auth 接口返回） |
| `playerName` | string | 否 | 角色名 |
| `channelUserId` | string | 否 | 渠道用户ID |
| `channelKey` | string | 是 | 渠道标识 |
| `serverId` | int | 是 | 服务器ID |
| `regionCode` | string | 否 | 分区CODE |
| `price` | string | 是 | 金额（如 `"6.00"`） |
| `refId` | string | 是 | 商品ID（productId） |
| `os` | string | 否 | 操作系统 |
| `sign` | string | 是 | MD5签名 |
| `serverUrl` | string | 否 | 游戏服兑换地址（可选，不传则自动从 game_server 表读取） |

### 签名算法

```
sign = MD5(playerId + identityId + serverId + price + CLIENT_KEY)
```

`CLIENT_KEY` 为后台配置的客户端密钥。

### 请求示例

```json
{
  "playerId": 1001,
  "identityId": "a1b2c3d4e5f6",
  "channelKey": "huawei",
  "serverId": 1,
  "regionCode": "rg_test",
  "price": "6.00",
  "refId": "product_001",
  "sign": "md5sign..."
}
```

### 响应

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "orderId": "ORDER20260527123456",
    "exchangeUrl": "http://game-server-address/exchange",
    "amount": 6.00
  }
}
```

### 响应字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| `orderId` | string | 订单ID（业务主键） |
| `exchangeUrl` | string | 游戏服兑换地址（取自 `game_server.backend_url`） |
| `amount` | number | 订单金额 |

---

## 2. 查询订单

### 请求

```
GET /bridge/order/{orderId}
```

### 请求参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `orderId` | string | 是 | 订单ID（路径参数） |

### 响应

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "orderId": "ORDER20260527123456",
    "payStatus": 10,
    "amount": 6.00,
    "productName": "60钻石",
    "serverId": 1,
    "playerId": 1001
  }
}
```

### payStatus 说明

| 值 | 说明 |
|----|------|
| 0 | 初始（待支付） |
| 5 | 已确认-兑换中 |
| 10 | 兑换成功 |
| 15 | 补单成功 |
| 20 | 兑换失败 |

---

## 3. 补单（救援）

当订单兑换失败时，调用此接口重新发起兑换。

### 请求

```
POST /bridge/order/{orderId}/rescue
```

### 请求参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `orderId` | string | 是 | 订单ID（路径参数） |

### 响应

```json
{
  "code": 200,
  "msg": "success",
  "data": { ... 订单信息 ... }
}
```

### 错误码

| code | msg | 说明 |
|------|-----|------|
| 500 | 订单不存在 | orderId 无效 |
| 500 | 订单已完成，无需补单 | 状态 >= 10 |
| 500 | 当前订单状态不允许补单 | 其他不允许的状态 |
