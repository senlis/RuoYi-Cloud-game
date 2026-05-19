# 网关 Nacos 配置说明

游戏模块的 Controller 放在 `ruoyi-system` 服务中，前端请求路径使用 `/system/game/...` 前缀。

## 无需新增独立路由

因前端请求路径为 `/system/game/...`，网关已有的 `/system/**` 路由（已配置 `StripPrefix=1`）已经能正确处理：

```
前端请求 → /system/game/project/list
  ↓ 网关匹配 /system/**，StripPrefix=1
  ↓ 转发到 ruoyi-system
控制器 → @RequestMapping("/game/project")
  ↓
响应
```

## 需要添加的配置

### 1. 添加客户端配置免认证路径

在 `security.ignore.whites` 中添加：

```yml
security:
  ignore:
    whites:
      - /auth/logout
      - /auth/login
      - /auth/register
      - /*/v2/api-docs
      - /*/v3/api-docs
      - /csrf
      - /system/game/*/clientConfig    # ← 新增：客户端获取区服配置（无需认证）
      - /bridge/health                  # ← 新增：桥接服务健康检查
      - /bridge/pay/*/notify            # ← 新增：渠道支付回调（无需认证）
```
