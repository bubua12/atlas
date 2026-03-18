# Atlas 项目日志规范文档

本文档旨在规范 Atlas 项目中的日志打印标准，确保日志的清晰性、可读性和实用性，便于系统监控、问题排查和业务审计。

## 1. 概述

日志是系统运行的“黑匣子”，在分布式架构中尤为重要。合理的日志记录可以帮助我们：
*   **快速定位问题**：通过错误堆栈和上下文信息还原现场。
*   **业务审计**：记录关键业务操作（谁、在什么时候、做了什么）。
*   **性能监控**：分析接口耗时和系统瓶颈。

## 2. 日志框架与配置

### 2.1 技术栈
项目统一采用 **SLF4J** (Simple Logging Facade for Java) 作为日志门面，**Logback** 作为默认日志实现（Spring Boot 默认集成）。

### 2.2 配置文件
日志配置主要通过 `application.yml` 或 `logback-spring.xml` 进行管理。

*   **开发环境 (dev)**：
    *   级别：`INFO` (核心业务) / `DEBUG` (调试模块)
    *   输出：控制台 (Console)
    *   MyBatis SQL：建议开启 `DEBUG` 或使用 `StdOutImpl` 以便查看 SQL 语句。

*   **生产环境 (prod)**：
    *   级别：`INFO`
    *   输出：文件 (File) / ELK 收集
    *   策略：按天滚动，保留 30 天日志。

**配置示例 (`application.yml`)**:
```yaml
logging:
  level:
    root: INFO
    com.bubua12.atlas: DEBUG # 开发环境可开启项目包的 DEBUG
  file:
    name: logs/atlas-server.log
mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl # 仅限开发环境
```

## 3. 日志分类与使用规范

### 3.1 应用日志 (Application Log)

用于记录系统运行状态、调试信息和异常堆栈。

*   **使用方式**：使用 Lombok 的 `@Slf4j` 注解。
*   **禁止**：使用 `System.out.println()` 或 `e.printStackTrace()`。

#### 3.1.1 日志级别标准

| 级别 | 描述 | 适用场景 |
| :--- | :--- | :--- |
| **ERROR** | 错误事件 | 影响系统正常运行的异常，需要人工介入处理。如：数据库连接失败、关键业务逻辑崩溃。 |
| **WARN** | 警告事件 | 不影响系统运行但需关注的异常或潜在风险。如：参数校验失败、非核心服务调用超时。 |
| **INFO** | 重要信息 | 关键业务流程节点、系统启动/关闭、定时任务执行情况。 |
| **DEBUG** | 调试信息 | 开发调试阶段的详细信息，如：变量值、复杂逻辑的分支走向。生产环境通常关闭。 |

#### 3.1.2 代码示例

**正确示例**：
```java
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    public void updateUser(UserDTO userDto) {
        log.info("开始更新用户信息, userId: {}", userDto.getId());
        try {
            // 业务逻辑...
            if (checkStatus(userDto)) {
                log.warn("用户状态异常, userId: {}, status: {}", userDto.getId(), userDto.getStatus());
            }
        } catch (Exception e) {
            // 必须记录异常堆栈 e
            log.error("更新用户信息失败, userId: {}", userDto.getId(), e);
            throw e;
        }
    }
}
```

**常见错误**：
1.  **字符串拼接**：`log.info("用户ID:" + id)` ❌ -> `log.info("用户ID: {}", id)` ✅ (性能更好)
2.  **丢失堆栈**：`log.error("错误: " + e.getMessage())` ❌ -> `log.error("错误", e)` ✅
3.  **无意义日志**：`log.info("start")` ❌ -> `log.info("开始处理订单: {}", orderId)` ✅

### 3.2 操作日志 (Operation Log)

用于记录用户的关键业务操作（审计日志），存储在数据库 `sys_oper_log` 表中。

*   **适用场景**：增、删、改等关键操作。
*   **核心组件**：`atlas-common-log` 模块。

#### 3.2.1 使用方式
在 Controller 方法上添加 `@OperLog` 注解。

*   `title`: 模块名称
*   `businessType`: 业务类型 (如 "新增", "修改", "删除", "授权")

#### 3.2.2 代码示例

```java
@RestController
@RequestMapping("/system/user")
public class SysUserController {

    @OperLog(title = "用户管理", businessType = "新增")
    @PostMapping
    public CommonResult<Void> add(@RequestBody SysUser user) {
        return sysUserService.add(user);
    }

    @OperLog(title = "用户管理", businessType = "强制下线")
    @DeleteMapping("/kickout/{token}")
    public CommonResult<Void> kickOut(@PathVariable String token) {
        // ...
    }
}
```

#### 3.2.3 自动记录字段
切面 `OperLogAspect` 会自动记录以下信息，无需手动处理：
*   操作人员 (OperName)
*   请求 IP (OperIp)
*   请求方法 (Method)
*   请求参数 (OperParam) - *自动截取前2000字符*
*   返回结果 (JsonResult) - *自动截取前2000字符*
*   执行状态与耗时 (Status, CostTime)
*   错误信息 (ErrorMsg)

## 4. 异常处理与日志

### 4.1 全局异常处理
项目使用 `GlobalExceptionHandler` (`atlas-common-web`) 统一处理异常。
*   **自动记录**：该处理器会自动通过 `log.error` 记录 `BusinessException` 和 `Exception` 的堆栈信息。
*   **规范**：业务代码中捕获异常后，如果无法自我恢复，应抛出 `BusinessException` 或原样抛出，交由全局处理器记录日志，**避免重复打印日志**（即不要 catch 打印了 error 又 throw）。

### 4.2 切面异常处理注意
在编写自定义切面（Aspect）时，**严禁**直接捕获异常并包装为 `RuntimeException` 抛出，这会导致全局异常处理器无法识别原始业务异常类型（如 `AuthException`）。

**错误写法**：
```java
try {
    return pjp.proceed();
} catch (Throwable e) {
    throw new RuntimeException(e); // ❌ 导致原始异常类型丢失
}
```

**正确写法**：
```java
try {
    return pjp.proceed();
} catch (Throwable e) {
    if (e instanceof RuntimeException) {
        throw (RuntimeException) e; // ✅ 保持原有运行时异常
    }
    throw new RuntimeException(e);
}
```

## 5. 敏感信息与安全

1.  **脱敏处理**：日志中**严禁**明文打印密码、密钥、身份证号、银行卡号等敏感信息。
    *   `@OperLog` 目前记录了请求参数，后续需增强对敏感字段（如 `password`）的脱敏过滤。
2.  **日志文件安全**：生产环境日志文件应设置合理的访问权限，防止未授权访问。

## 6. 链路追踪 (建议)

为便于在微服务架构中排查跨服务调用问题，建议后续引入 **TraceId**。
*   **MDC 机制**：在请求入口（Gateway/Filter）生成唯一的 TraceId 放入 `MDC`。
*   **日志格式**：配置 Logback pattern 包含 `%X{traceId}`。
*   **Feign 透传**：在服务间调用时将 TraceId 放入 Header 传递。

---
*文档维护人: Atlas 开发团队*
*最后更新日期: 2026-03-18*
