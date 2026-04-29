# AGENTS.md

本文件为 Qoder (qoder.com) 在本仓库中工作时提供指导。

## 构建与运行命令

### 后端 (Maven, JDK 17+)

```bash
# 打包所有模块（跳过测试 - 项目无测试用例）
mvn clean package -Dmaven.test.skip=true

# 打包单个模块（以 system 为例，-am 同时构建依赖模块）
mvn clean package -pl ruoyi-modules/ruoyi-system -am -Dmaven.test.skip=true

# 运行单个服务（在项目根目录执行）
java -Xms512m -Xmx1024m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=512m -Dfile.encoding=utf-8 -jar ruoyi-auth/target/ruoyi-auth.jar
```

Windows 批处理脚本位于 `bin/` 目录：`run-gateway.bat`、`run-auth.bat`、`run-modules-system.bat`、`run-modules-gen.bat`、`run-modules-job.bat`、`run-modules-file.bat`、`run-monitor.bat`。

### 前端 (ruoyi-ui/, Vue 2 + Element UI)

```bash
cd ruoyi-ui
npm install
npm run dev          # 开发服务器，端口 80
npm run build:prod   # 生产构建 -> dist/
```

环境变量文件：`.env.development`（API 前缀 `/dev-api`）、`.env.production`（`/prod-api`）。

### 基础设施 (Docker)

```bash
cd docker
# 先启动基础服务
docker-compose up -d ruoyi-mysql ruoyi-redis ruoyi-nacos
# 再启动应用服务
docker-compose up -d ruoyi-gateway ruoyi-auth ruoyi-modules-system ruoyi-nginx
```

依赖基础设施：MySQL 5.7（端口 3306）、Redis（端口 6379）、Nacos 3.x（端口 8848）。

## 架构概览

本项目是基于 Spring Cloud 的微服务管理平台（RuoYi-Cloud v3.6.8），请求链路如下：

```
客户端 -> Nginx(:80) -> Gateway(:8080) -> Auth(:9200) / System(:9201) / Gen(:9202) / Job(:9203) / File(:9300)
                                              |
                                        Nacos(:8848) -- 服务注册与配置中心
                                              |
                                    Redis(:6379) -- JWT 令牌缓存与会话
```

### 服务端口映射

| 服务 | 端口 | 模块路径 |
|------|------|----------|
| 网关 | 8080 | `ruoyi-gateway/` |
| 认证中心 | 9200 | `ruoyi-auth/` |
| 系统模块 | 9201 | `ruoyi-modules/ruoyi-system/` |
| 代码生成 | 9202 | `ruoyi-modules/ruoyi-gen/` |
| 定时任务 | 9203 | `ruoyi-modules/ruoyi-job/` |
| 文件服务 | 9300 | `ruoyi-modules/ruoyi-file/` |
| 监控中心 | 9100 | `ruoyi-visual/ruoyi-monitor/` |

### 认证流程

1. Gateway 的 `AuthFilter`（全局过滤器，order -200）拦截所有请求，白名单路径放行
2. 从 `Authorization` 请求头提取 JWT 令牌，通过 `JwtUtils` 解析 Claims
3. 校验 Redis 中令牌是否存在（key 为 `login_tokens:<userkey>`）
4. 将 `user_key`、`details_user_id`、`details_username` 注入请求头，传递给下游服务
5. 清除 `from-source` 请求头，防止外部请求伪装内部调用
6. 下游服务通过 `SecurityContextHolder`（线程本地变量，由 `HeaderInterceptor` 填充）获取用户上下文

### 服务间通信

服务间通过 OpenFeign 通信。Feign 接口定义在 `ruoyi-api/ruoyi-api-system/`，配有 FallbackFactory 实现熔断降级。

调用模式：`@FeignClient(value = ServiceNameConstants.SYSTEM_SERVICE, fallbackFactory = RemoteXxxFallbackFactory.class)`

服务名定义在 `ServiceNameConstants` 中：`ruoyi-auth`、`ruoyi-system`、`ruoyi-file`。

仅供内部调用的接口使用 `@InnerAuth` 注解 + `from-source` 请求头校验，防止外部访问。`FeignRequestInterceptor` 在发出 Feign 请求时自动添加该请求头。

## 模块依赖结构

```
ruoyi-common（公共模块）
    ├── common-core         -- 基础类、R<T> 统一响应、常量、JWT 工具、Excel 注解
    ├── common-redis        -- RedisService 封装
    ├── common-security     -- TokenService、鉴权注解、GlobalExceptionHandler、Feign 配置
    ├── common-log          -- @Log 注解 + AOP 操作日志记录
    ├── common-datascope    -- @DataScope 注解 + AOP 行级数据权限过滤
    ├── common-datasource   -- Druid + dynamic-datasource 多数据源支持
    ├── common-sensitive    -- @Sensitive 注解，响应数据脱敏
    ├── common-seata        -- Seata 分布式事务集成
    └── common-swagger      -- SpringDoc OpenAPI 自动配置

ruoyi-api（Feign 接口定义）
    └── ruoyi-api-system    -- RemoteUserService、RemoteFileService、RemoteLogService + 领域对象

ruoyi-modules（业务服务，依赖 common + api）
ruoyi-gateway（仅依赖 common-core + common-redis，基于 WebFlux）
ruoyi-auth（依赖 common-security）
```

## 关键模式与约定

### 应用启动注解

每个服务的 Application 类均标注：
- `@EnableCustomConfig` -- 组合注解：启用 AOP 代理暴露、`@MapperScan("com.ruoyi.**.mapper")`、异步执行，并导入 `ApplicationConfig` + `FeignAutoConfiguration`
- `@EnableRyFeignClients` -- 启用 Feign，扫描基础包 `com.ruoyi`

### Nacos 配置管理

本地 `bootstrap.yml` 仅包含：端口、应用名、Nacos 地址。所有业务配置存放在 Nacos 配置中心。每个服务导入两个配置文件：
1. `application-${profile}.yml` -- 公共配置（数据源、Redis 等）
2. `${spring.application.name}-${profile}.yml` -- 服务专属配置

### 后端分层约定

每个业务模块遵循：`controller -> service(impl) -> mapper(接口) -> mapper XML`

- Controller 层：`com.ruoyi.{module}.controller`
- Service 层：`com.ruoyi.{module}.service` / `com.ruoyi.{module}.service.impl`
- Mapper 层：`com.ruoyi.{module}.mapper`（Java 接口）+ `resources/mapper/{module}/XxxMapper.xml`（SQL 映射）
- 领域对象：`com.ruoyi.{module}.domain`

### 权限与数据范围

- 方法级权限：`@RequiresPermissions("system:user:list")`、`@RequiresRoles("admin")`
- 数据级权限：`@DataScope(deptAlias = "d", userAlias = "u")` -- 通过 AOP 注入 SQL WHERE 条件，按部门/用户层级过滤数据
- 登录校验：`@RequiresLogin`

### 统一响应格式

所有 REST 接口返回 `R<T>`（来自 common-core）或 `AjaxResult`。使用 `R.ok(data)` / `R.fail(msg)`。

### 数据库

SQL 初始化脚本在 `sql/` 目录：`ry_20260417.sql`（主库表结构）、`ry_config_20260311.sql`（Nacos 配置库）、`quartz.sql`（调度器表）、`ry_seata_20210128.sql`（Seata undo_log）。

ORM 使用 MyBatis + XML 映射文件，分页通过 PageHelper 实现。

### 前端结构

- API 调用：`src/api/`（按模块组织：system、monitor、tool）
- 页面视图：`src/views/`（与路由结构对应）
- 公共组件：`src/components/`（DictData、FileUpload、ImagePreview、Pagination 等）
- 权限指令：`v-hasPermi`、`v-hasRole`，定义在 `src/directive/`
- 状态管理：Vuex store，位于 `src/store/modules/`
- 工具函数：`src/utils/`（request.js 封装 axios，自动注入令牌和统一错误处理）
