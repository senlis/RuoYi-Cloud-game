# 渠道认证接口

## 概述

玩家在游戏客户端启动时，SDK 认证通过后会调用此接口，在后台登录或创建用户账号，并返回平台唯一标识。

## 请求

### 路径

```
GET /bridge/auth
```

### 请求参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `channelKey` | string | 是 | 渠道标识（如 `huawei`、`oppo`） |
| `userId` | string | 是 | 渠道用户ID |
| `serverId` | int | 是 | 服务器ID |
| `packageId` | string | 否 | 包ID |
| `sign` | string | 是 | 签名（由渠道SDK生成） |
| `token` | string | 否 | 渠道认证令牌 |
| `timestamp` | long | 否 | 时间戳 |

### 请求示例

```
GET /bridge/auth?channelKey=huawei&userId=123456&serverId=1&sign=xxx&timestamp=1700000000000
```

## 响应

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "identityId": "a1b2c3d4e5f6...",
    "userId": "1",
    "serverId": 1,
    "channelKey": "huawei",
    "sign": "md5签名...",
    "timestamp": 1700000000000
  }
}
```

### 响应字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| `identityId` | string | 平台唯一标识（UUID，创建时生成） |
| `userId` | string | 用户ID（数据库自增主键） |
| `serverId` | int | 服务器ID |
| `channelKey` | string | 渠道标识 |
| `sign` | string | MD5签名（服务端生成，用于游戏服验证） |
| `timestamp` | long | 时间戳 |

### 响应签名算法

`sign` 由服务端生成，参数按 key 字母顺序排序后拼接 + payKey，取 MD5：

```
sign = MD5(channelKey + value + identityId + value + serverId + value + timestamp + value + userId + value + payKey)
```

其中 `payKey` 取自渠道配置（`br_channel_arg_config.pay_key`）。
