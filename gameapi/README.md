# 游戏服务器 API 接口规范

本目录定义管理后台向游戏服务器推送数据时需要遵循的接口规范。游戏服务器需按此处定义的路径、参数、签名规则实现对应的 HTTP 接口。

## 通信方式

- **协议**: HTTP/HTTPS
- **方法**: POST
- **Content-Type**: `application/json;charset=utf-8`
- **后端地址**: 从 `game_server.backend_url` 字段读取，推送时拼接 API 路径
- **超时**: 连接 5s，读取 10s

## 签名规则

所有推送到游戏服务器的请求都包含 `sign` 参数，用于验证请求合法性。

### 签名算法

1. 将所有参数（不含 `sign` 本身）按 key 的字母顺序升序排列
2. 拼接为 `key1 + value1 + key2 + value2 + ... + md5Key`
3. 取 MD5 哈希（32位小写十六进制）

```
sign = MD5(key1 + value1 + key2 + value2 + ... + md5Key)
```

### md5Key 配置

`md5Key` 存储在 `game_project.dynamic_fields` 的 JSON 中，字段名为 `md5key`，需在项目管理页面配置。

## API 列表

| 路径 | 说明 | 文档 |
|------|------|------|
| `POST {backendUrl}/mail` | GM 邮件推送 | [mail.md](mail.md) |
| `POST {backendUrl}/resource` | 资源发放推送 | [resource.md](resource.md) |

## 通用返回格式

游戏服务器接收到请求后，应返回 HTTP 状态码 `200` 表示成功接收，非 `200` 视为推送失败。

```json
// 成功
HTTP 200

// 失败
HTTP 500
```
