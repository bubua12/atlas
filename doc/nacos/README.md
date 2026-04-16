# Nacos 配置中心模板

这组文件用于初始化 Atlas 的 Nacos 配置中心。

推荐在 Nacos 中使用如下 DataId：

- `atlas-common.yaml`
- `atlas-logging-dev.yaml`
- `atlas-logging-prod.yaml`
- `atlas-auth.yaml`
- `atlas-gateway.yaml`
- `atlas-infra.yaml`
- `atlas-monitor.yaml`
- `atlas-system.yaml`

推荐 Group：

- `ATLAS`

推荐导入顺序：

1. `atlas-common.yaml`
2. `atlas-logging-${ATLAS_PROFILE}.yaml`
3. `${spring.application.name}.yaml`

说明：

- `atlas-common.yaml` 放公共稳定默认值。
- `atlas-common.yaml` 现在包含了公共 `MySQL`、`Redis`、`MyBatis-Plus` 与通用签名校验配置，模板里使用的是可直接导入的字面值，不再依赖环境变量展开。
- `atlas-logging-dev.yaml` / `atlas-logging-prod.yaml` 放环境级日志策略。
- 各服务 DataId 放服务独有配置，例如端口、网关路由、内部服务签名密钥、OAuth 配置、Knife4j、上传配置等，优先级高于公共配置。
- 本地 `application.yml` 现在只保留应用名、profile 与 `spring.cloud.nacos.*` 等最小启动配置；其余可远程化配置统一从 Nacos 获取。
- `spring.cloud.nacos.*` 这类“连接 Nacos 自己”的配置不能放到 Nacos 里，仍需保留在本地 `application.yml`。

可通过 Nacos 控制台手工创建这些 DataId，也可以用接口导入。示例：

```powershell
.\doc\nacos\import-nacos-config.ps1
```

说明：

- `ATLAS_NACOS_HOST` 可以传 `10.0.8.132`，也可以传 `10.0.8.132:8848`；脚本会在缺省时自动补 `8848` 端口。
- 导入脚本会根据文件扩展名自动设置 Nacos 配置类型，例如 `.yaml/.yml -> yaml`，避免控制台把内容识别成普通文本。
