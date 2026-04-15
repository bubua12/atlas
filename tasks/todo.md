# 安全改造任务清单

- [x] 检查当前已存在的安全请求头改动，并与本次实现保持兼容
- [x] 增加统一请求头常量与签名载荷模型
- [x] 增加网关用户身份签名与内部服务认证所需的签名服务和安全配置
- [x] 改造网关 `AuthFilter`，下发带签名的用户身份断言
- [x] 增加 MVC 拦截器，验证网关签名并建立 `SecurityContextHolder`
- [x] 改造 `PreAuthorizeAspect`，优先使用已验证上下文并保留兼容回退
- [x] 增加内部服务认证注解/切面与 Feign 请求拦截器
- [x] 为 `atlas-system` 的内部 Feign 接口增加服务白名单保护
- [x] 执行聚焦验证，覆盖编译、现有测试与兼容路径
- [x] 为新增安全链路补充维护型注释，说明签名协议、执行边界与拦截顺序

## 评审记录

- 已完成网关签名用户断言、下游验签建上下文、内部 Feign 服务认证与内部接口白名单保护。
- 已补充核心安全链路注释，重点覆盖网关签名协议、下游验签入口、内部服务认证与 MVC 拦截顺序。
- 已执行 `mvn -pl atlas-gateway,atlas-auth,atlas-system,atlas-infra,atlas-monitor -am -DskipTests compile`，构建通过。
- 已执行 `mvn -pl atlas-gateway,atlas-auth,atlas-system,atlas-infra,atlas-monitor -am test`，现有测试通过。
- 当前未新增专门的安全单元测试，后续可补充针对签名校验与重放防护的独立测试用例。
