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

# Nacos 配置中心与 SQL 日志分层任务
- [x] 为各服务引入 Nacos Config，接入配置中心能力
- [x] 将公共与环境级日志配置改造为 Nacos 远程配置模板
- [x] 调整服务本地配置，保留本地默认值并通过 Nacos 控制 SQL 日志级别
- [x] 执行构建验证并记录结果

## 评审记录

- 已为 `atlas-gateway`、`atlas-auth`、`atlas-system`、`atlas-infra`、`atlas-monitor` 引入 `spring-cloud-starter-alibaba-nacos-config`，并在本地 `application.yml` 中统一接入 `atlas-common.yaml`、`atlas-logging-${ATLAS_PROFILE}.yaml` 与服务级 DataId。
- 已保留服务本地 `application.yml` 的基础兜底配置，Nacos 远程配置主要承接公共默认值、dev/prod 日志级别与服务级差异配置。
- 已新增 `doc/nacos/configs` 下的 Nacos 配置模板，以及 `doc/nacos/import-nacos-config.ps1` 导入脚本，便于将仓库模板快速发布到配置中心。
- 已验证 `atlas-system` 去掉 `StdOutImpl` 后，需通过 `logging.level.com.bubua12.atlas.system.mapper` 控制 SQL 日志输出；对应 dev/prod 模板已分别给出 `debug` 与 `info` 策略。
- 已执行 `mvn -pl atlas-common/atlas-common-database,atlas-gateway,atlas-auth,atlas-system,atlas-infra,atlas-monitor -am test -DskipTests=false`，构建与测试通过。
- 当前环境中的 Nacos 已存在 `atlas-common.yaml`、`atlas-logging-dev.yaml`、`atlas-auth.yaml` 等 DataId，但内容为空；本次未直接覆盖远程配置，需按需执行导入脚本或在控制台粘贴模板内容。
- 已将公共 `MySQL`、`Redis` 配置补入 `doc/nacos/configs/atlas-common.yaml`，导入后可由 Nacos 统一管理；服务本地 `application.yml` 暂时保留同值兜底，避免远程配置缺失时无法启动。
- 已按最新要求将 `doc/nacos/configs/atlas-common.yaml` 中的 MySQL/Redis 改为字面值配置，避免 Nacos 远程配置依赖环境变量展开。
- 已进一步将各服务可远程化的端口、路由、签名配置、OAuth 配置、Knife4j、上传配置等迁入对应 Nacos DataId，本地 `application.yml` 仅保留连接 Nacos 所需的最小配置。
