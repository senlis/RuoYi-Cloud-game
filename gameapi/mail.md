# GM 邮件推送接口

## 概述

管理后台 GM 邮件模块审核通过后，向游戏服务器推送邮件内容，由游戏服负责发放给玩家。

## 请求

### 路径

```
POST {backendUrl}/mail
```

### 请求参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `mailId` | long | 是 | 邮件ID |
| `title` | string | 是 | 邮件标题 |
| `content` | string | 是 | 邮件内容（富文本HTML） |
| `targetType` | int | 是 | 目标范围 0=全服 1=条件筛选 2=指定玩家 |
| `expireDays` | int | 是 | 有效期天数 |
| `serverId` | int | 是 | 当前推送的目标服务器ID |
| `minLevel` | int | 否 | 最低等级（targetType=1时） |
| `maxLevel` | int | 否 | 最高等级（targetType=1时） |
| `minVip` | int | 否 | 最低VIP（targetType=1时） |
| `maxVip` | int | 否 | 最高VIP（targetType=1时） |
| `targetPlayers` | string | 否 | 指定角色ID列表（targetType=2时，逗号分隔） |
| `rewards` | string | 否 | 附件奖励JSON `[{itemId,itemName,count}]` |
| `sign` | string | 是 | MD5签名 |

### 请求示例

```json
{
  "mailId": 1,
  "title": "五一活动奖励",
  "content": "<h1>恭喜</h1><p>获得五一节日奖励</p>",
  "targetType": 0,
  "expireDays": 7,
  "serverId": 1,
  "rewards": "[{\"itemId\":27000012,\"itemName\":\"一箱金币\",\"count\":1}]",
  "sign": "a1b2c3d4e5f6..."
}
```

### 签名示例

假设参数为：
```
content=<h1>恭喜</h1>
expireDays=7
mailId=1
rewards=[{"itemId":27000012,"itemName":"一箱金币","count":1}]
serverId=1
targetType=0
title=五一活动奖励
```

密钥为 `abc123`，则签名字符串为：
```
content<h1>恭喜</h1>expireDays7mailId1rewards[{"itemId":27000012,"itemName":"一箱金币","count":1}]serverId1targetType0title五一活动奖励abc123
```

`sign = MD5(签名字符串)`

## 响应

| 状态码 | 说明 |
|--------|------|
| 200 | 邮件接收成功 |
| 非200 | 推送失败（管理后台会记录并允许重推） |
