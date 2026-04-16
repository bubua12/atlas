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
- [x] 为关键认证授权代码补充维护型注释，重点覆盖 `JwtUtils` 与认证主链入口
- [x] 将内部 Feign 签名拦截器从 `atlas-auth` 上移到公共安全模块，避免公共能力落在单个业务服务里
- [x] 将内部 Feign 签名时机从模板阶段调整到最终请求阶段，修复类级 path 未参与签名的问题

## 评审记录

- 已完成网关签名用户断言、下游验签建上下文、内部 Feign 服务认证与内部接口白名单保护。
- 已补充核心安全链路注释，重点覆盖网关签名协议、下游验签入口、内部服务认证与 MVC 拦截顺序。
- 已补充 `JwtUtils` 等关键认证授权代码注释，明确 JWT 解析、Redis 登录态校验与兼容回退之间的职责边界。
- 已将内部 Feign 请求签名拦截器迁移到公共安全自动配置，后续新增 Feign 调用服务时无需再复制局部配置。
- 已将内部 Feign 请求签名改为在最终 `Request` 阶段执行，避免 `@FeignClient(path=...)` 前缀未参与签名导致验签失败。
- 已执行 `mvn -pl atlas-gateway,atlas-auth,atlas-system,atlas-infra,atlas-monitor -am -DskipTests compile`，构建通过。
- 已执行 `mvn -pl atlas-gateway,atlas-auth,atlas-system,atlas-infra,atlas-monitor -am test`，现有测试通过。
- 当前未新增专门的安全单元测试，后续可补充针对签名校验与重放防护的独立测试用例。
# MyBatis-Plus 日志统一收口任务
- [x] 梳理各服务 MyBatis-Plus 日志配置现状，确认公共收口方案
- [x] 在 `atlas-common-database` 中补充 MyBatis-Plus 日志默认自动配置
- [x] 补充测试验证默认配置与服务侧覆盖行为
- [x] 执行针对性构建/测试并记录结论

## 评审记录

- 已确认当前 MyBatis-Plus 配置分散在 `atlas-system` 与 `atlas-infra` 的服务配置中，公共模块更适合通过自动配置收口默认日志实现，而不是依赖公共 `application.yml`。
- 已在 `atlas-common-database` 中新增 `MybatisPlusPropertiesCustomizer`，默认将 `mybatis-plus.configuration.log-impl` 收口为 `Slf4jImpl`，同时保留服务侧显式覆盖能力。
- 已补充 `AtlasDataBaseAutoConfigurationTest`，验证“未配置时走统一默认值”和“业务显式配置时保持原值”两条路径。
- 已执行 `mvn -pl atlas-common/atlas-common-database -am test -DskipTests=false`，测试通过。
