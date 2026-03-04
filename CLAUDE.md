# CLAUDE.md

本文件为 Claude Code (claude.ai/code) 在此代码库中工作时提供指导。

## 项目概述

Atlas 是基于 JDK 21 构建的 Spring Boot 3 + Spring Cloud 微服务平台，提供认证授权、系统管理、监控、代码生成等企业级能力。

**核心技术栈：**
- Spring Boot 3.2.3 / Spring Cloud 2023.0.0 / Spring Cloud Alibaba 2023.0.1.0
- JDK 21, Maven 3.9+
- MyBatis-Plus 3.5.5, MySQL 8.x, Redis 7.x + Redisson
- Nacos 2.x（服务发现与配置中心）, Spring Cloud Gateway
- JWT 认证（jjwt 0.12.5）

## 构建与运行命令

**构建整个项目：**
```bash
mvn clean install -DskipTests
```

**构建指定模块：**
```bash
mvn clean install -pl atlas-auth -am -DskipTests
```

**运行测试：**
```bash
mvn test
```

**运行指定服务（示例）：**
```bash
# IDE 中：直接运行 Application 启动类
# 命令行：
java -jar atlas-gateway/target/atlas-gateway-1.0.0.jar
java -jar atlas-auth/target/atlas-auth-1.0.0.jar
java -jar atlas-system/atlas-system-biz/target/atlas-system-biz-1.0.0.jar
```

**服务启动顺序：** Gateway → Auth → System → Infra → Monitor

## 架构说明

**微服务及端口：**
- `atlas-gateway` (8080) - API 网关，统一入口
- `atlas-auth` (9100) - 认证授权服务
- `atlas-system` (9200) - 系统管理（用户、角色、菜单、部门、字典）
- `atlas-infra` (9300) - 基础设施（文件上传、代码生成）
- `atlas-monitor` (9400) - 监控服务

**模块结构：**
- `atlas-dependencies` - BOM 依赖版本统一管理
- `atlas-common` - 公共模块（core、web、redis、mybatis、security、log）
- 业务服务遵循 `xxx-api`（Feign 接口 + DTO）+ `xxx-biz`（实现）模式

**关键设计模式：**
- 服务间调用使用 `xxx-api` 模块中定义的 Feign 客户端
- 所有 API 响应使用统一的 `R<T>` 包装，包含 `code`、`msg`、`data` 字段
- 网关路由所有请求：`/auth/**` → atlas-auth，`/system/**` → atlas-system，以此类推

## 权限系统

项目使用 `@RequiresPermission` 注解通过 AOP 实现访问控制。

**工作流程：**
1. `PreAuthorizeAspect` 切面拦截所有标有 `@RequiresPermission` 的方法
2. 从 `SecurityContextHolder`（ThreadLocal）获取当前用户 Token
3. 从 Redis 中获取用户权限列表
4. 超级管理员（User ID = 1）直接放行所有请求
5. 普通用户必须在权限集合中包含指定权限
6. 权限校验失败时抛出 `BusinessException` (403)

**在 Controller 中使用：**
```java
@RequiresPermission("system:user:list")
@GetMapping("/list")
public R<List<SysUser>> list() { ... }
```

**权限标识命名规范：** `模块:资源:操作`（如 `system:user:add`、`monitor:server:view`）

**数据库配置：** 权限存储在 `sys_menu` 表的 `perms` 字段，通过 `sys_role_menu` 表关联到角色。

**问题排查：**
- 检查 Redis 中 `auth:token:xxx` 键，确认用户权限集合是否包含所需权限
- 确保权限字符串完全匹配（区分大小写，无空格）
- 确认 Controller 方法为 `public` 且类标注了 `@RestController`
- 确保网关正确传递 `Authorization` 请求头
- 确认 `UserContextInterceptor` 正常填充 ThreadLocal 用户上下文

## 常用开发模式

**操作日志：** 在 Controller 方法上使用 `@OperLog` 注解，通过 AOP 自动记录操作日志。

**分页查询：** 使用 `PageQuery` 类接收 Controller 中的分页参数。

**模块依赖关系：**
- 业务服务依赖 `atlas-common-web`、`atlas-common-redis`、`atlas-common-mybatis`、`atlas-common-security`、`atlas-common-log`
- 网关仅依赖 `atlas-common-core`
- `atlas-common-security` 依赖 `atlas-common-redis` 用于 Token 存储

## 配置说明

各服务的主配置文件位于 `src/main/resources/application.yml`。

**关键配置项：**
- Nacos：`spring.cloud.nacos.discovery.server-addr`（默认：127.0.0.1:8848）
- 数据库：`spring.datasource.url`（默认：jdbc:mysql://127.0.0.1:3306/atlas）
- Redis：`spring.data.redis.host`（默认：127.0.0.1）
- JWT：`atlas.jwt.secret`（生产环境必须修改）、`atlas.jwt.expiration`（默认：7200 秒）

## 开发规范

- 所有 Controller 方法返回统一的 `CommonResult<T>` 响应格式
- 服务间调用使用模块的 Feign 接口
- 需要权限控制的 Controller 方法添加 `@RequiresPermission` 注解
- 需要记录操作日志的 Controller 方法添加 `@OperLog` 注解
- 超级管理员（User ID = 1）默认拥有所有权限


---

## **Workflow Orchestration (工作流编排)**

### **1. Plan Node Default (默认计划节点)**

* 对于任何非琐碎任务（3步以上或涉及架构决策），进入计划模式。
* 如果执行过程中出现偏差，立即**停止**并重新制定计划——不要强行推进。
* 计划模式不仅用于构建，也用于验证步骤。
* 预先编写详细规范，以减少歧义。

### **2. Subagent Strategy (子代理策略)**

* 大量使用子代理以保持主上下文窗口的整洁。
* 将研究、探索和并行分析任务分配给子代理。
* 对于复杂问题，通过子代理投入更多计算资源。
* 每个子代理负责一个方向，以确保专注执行。

### **3. Self-Improvement Loop (自我完善循环)**

* 在收到用户的任何更正后：更新 `tasks/lessons.md` 并记录模式。
* 为自己编写规则，防止重复犯错。
* 无情地迭代这些经验教训，直到错误率下降。
* 在针对相关项目启动会话时，审查这些经验教训。

### **4. Verification Before Done (完成后验证)**

* 在证明任务有效之前，绝不标记为完成。
* 在相关时，对比主分支行为与你的更改之间的差异。
* 问问自己：“资深工程师会批准这个吗？”
* 运行测试，检查日志，演示正确性。

### **5. Demand Elegance (Balanced) (追求优雅 - 均衡)**

* 对于非琐碎的更改：停下来思考“是否有更优雅的方法？”
* 如果修复方案感觉很笨拙：“基于我目前所知，实现那个优雅的方案”。
* 简单、明显的修复可以跳过此步——不要过度设计。
* 在提交工作之前，先对自己进行挑战。

### **6. Autonomous Bug Fixing (自主 Bug 修复)**

* 当收到 Bug 报告时：直接修复它。不要请求手把手的指导。
* 定位日志、错误和失败的测试——然后解决它们。
* 用户无需进行上下文切换。
* 无需他人告知，主动修复失败的 CI 测试。

---

## **Task Management (任务管理)**

1. **Plan First (计划优先)**：将计划写入 `tasks/todo.md`，并附带可勾选的项目。
2. **Verify Plan (验证计划)**：在开始实施前进行确认。
3. **Track Progress (跟踪进度)**：随进度标记完成项。
4. **Explain Changes (解释变更)**：在每个步骤提供高层级的总结。
5. **Document Results (记录结果)**：在 `tasks/todo.md` 中添加评审章节。
6. **Capture Lessons (吸取教训)**：在更正后更新 `tasks/lessons.md`。

---

## **Core Principles (核心原则)**

* **Simplicity First (简单至上)**：使每一次更改尽可能简单。影响最小的代码。
* **No Laziness (拒绝懒惰)**：寻找根本原因。不接受临时修复。坚持资深开发人员的标准。
* **Minimal Impact (最小影响)**：更改应仅触及必要的部分。避免引入新 Bug。

---