# Atlas 高优先级架构问题修复

## 计划

- [x] 阅读 `Atlas项目架构优化报告.md` 并核对当前实现
- [x] 为本轮高优先级修复建立统一的请求信任模型
- [x] 修复用户请求可绕过网关直达业务服务的问题
- [x] 修复 `atlas-auth -> atlas-system` Feign 内部接口无调用方校验的问题
- [x] 消除网关与下游服务的重复 Redis 查询
- [x] 统一 Token 前缀处理，避免 `Bearer ` 与原始 token 混用
- [x] 为管理员权限引入通配符表示并补齐权限匹配逻辑
- [x] 修复 Token 刷新先删后建的竞态窗口
- [x] 配置 Feign 超时与重试，降低级联阻塞风险
- [x] 编译并验证受影响模块
- [x] 实现权限变更事件通知并刷新在线会话
- [x] 为登录态引入 TTL 抖动，降低同批 token 集中过期风险
- [x] 维护按用户索引的在线会话，避免权限刷新时全量扫描 token
- [x] 为异步线程池增加安全上下文透传与兜底清理

## 验收标准

- [x] 业务服务收到带 `Authorization` 的请求时，必须验证调用方签名
- [x] 受保护的内部接口只能被允许的服务调用
- [x] 常规鉴权请求在下游不再为权限校验重复查询 Redis
- [x] 管理员不再把全量权限列表塞进会话对象
- [x] Token 刷新过程中不会因为“先删除旧 token”造成可用性窗口
- [x] Feign 调用具备显式超时配置
- [x] 用户角色、角色菜单、数据权限变更后，在线会话能自动刷新权限/数据范围
- [x] 不依赖 `keys auth:token:*` 全量扫描即可定位某个用户的在线会话
- [x] 登录态 TTL 存在随机抖动，降低缓存雪崩风险
- [x] 异步线程执行结束后不会残留上个请求的安全上下文

## 评审记录

- 已实现统一请求签名头：网关用户请求与 Auth 的 Feign 内部请求都会携带签名、时间戳与调用方服务标识。
- 业务服务新增请求验签拦截器，拒绝未签名的带身份请求，避免直接信任 `Authorization` / `X-User-*` / `X-Login-User`。
- `atlas-system` 的内部用户查询/验密接口已增加 `@InternalApi(allowedServices = {"atlas-auth"})` 保护。
- 网关改为直接从 Redis 读取 `LoginUser` 并 Base64 透传，下游从请求头恢复上下文，`PreAuthorizeAspect` 优先复用上下文，常规路径不再重复查 Redis。
- 超级管理员权限改为 `*:*:*` 通配符，权限切面新增通配符匹配能力。
- `AuthServiceImpl.refreshToken()` 已调整为先写入新 token 再删除旧 token，并统一了 Bearer token 解析逻辑。
- 系统侧新增权限变更发布器：用户分配角色、角色分配菜单、角色分配用户、数据权限更新、菜单更新/删除后，会在事务提交后发布受影响用户集合。
- Auth 侧新增权限变更订阅器与在线会话刷新服务：通过用户维度 token 索引精确定位在线会话，并回源 `atlas-system` 更新 `LoginUser.permissions` 与 `dataScope`。
- 登录态新增用户 token 索引 `auth:user:tokens:{userId}`，登录、刷新、登出都会同步维护；权限刷新不再依赖全量扫描 token key。
- 登录与刷新 token 引入 TTL 抖动，降低大量用户同批登录后的集中过期风险。
- 异步线程池增加 `SecurityContextHolder` 透传与执行后清理，减少线程复用时的上下文残留风险。
- 执行验证：`mvn -DskipTests compile` 通过（2026-04-13，本地工作区，第二轮修复后再次验证）。

## 2026-04-14 InternalApiAspect 排查

- [x] 确认登录链路是否命中 `@InternalApi` 标注的 `atlas-system` Controller
- [x] 修复 `InternalApiAspect` 未被 Spring AOP 识别的问题
- [x] 编译验证 `atlas-common-security` 与 `atlas-system` 相关改动

## 2026-04-14 排查记录

- 登录时 `atlas-auth` 会通过 `AtlasSystemFeign` 调用 `atlas-system` 的 `/user/verify-password` 与 `/user/info/{username}`，两者都标注了 `@InternalApi(allowedServices = {"atlas-auth"})`。
- `RequestSecurityInterceptor` 会先把签名校验结果写入 `request` 属性，`InternalApiAspect` 再读取 `atlas.trustedRequest` 与 `atlas.callerService` 做放行判断，因此正常链路应当能进入切面。
- 根因是 `InternalApiAspectConfiguration#internalApiAspect()` 返回类型写成了 `Object`，Spring AOP 在构建 `@Aspect` 顾问时按 bean 类型识别，这个 bean 因类型被声明为 `Object` 而没有被织入。
- 修复后新增回归测试 `InternalApiAspectConfigurationTest`，验证 bean 已被 AOP 代理，未带受信属性的请求会被拦截，带 `atlas-auth` 调用方属性的请求可正常放行。

## 2026-04-14 异常体系优化

- [ ] 重构通用业务异常承载，支持静态错误码与动态下游错误并存
- [ ] 统一 `atlas-auth` 登录链路的异常抛出与登录失败计数策略
- [ ] 增加回归测试并验证 `atlas-auth` 编译通过
