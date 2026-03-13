# Atlas 并发模块 (atlas-common-concurrent) 使用指南

## 1. 模块简介
本模块 (`atlas-common-concurrent`) 旨在为整个微服务架构提供**统一、安全、可观测**的并发编程基础能力。

### 核心功能
1.  **统一线程池管理**：杜绝随意 `new Thread()`，防止资源耗尽。
2.  **上下文无感透传**：解决父子线程 `ThreadLocal` / `MDC` (TraceId) 丢失问题。
3.  **标准化配置**：提供 `ThreadPoolBuilder`，简化线程池创建。

---

## 2. 核心原理：为什么需要它？

### 2.1 痛点一：上下文丢失
在 Spring Boot 或 Java 原生线程池中，子线程无法继承父线程的 `ThreadLocal` 变量。
- **后果**：
    - 日志链路断裂：主线程有 `traceId`，子线程日志里没有，无法串联请求。
    - 鉴权失败：主线程有 `SecurityContext` (用户信息)，子线程拿不到，导致异步调用服务失败。

### 2.2 解决方案：装饰器模式 + TTL
我们引入了阿里开源的 `TransmittableThreadLocal (TTL)` 并结合 `MDC` 实现了自定义的 `AtlasThreadPoolTaskExecutor`。

**工作流程**：
1.  **捕获 (Capture)**：在任务提交 (`submit/execute`) 的瞬间，捕获当前主线程的 `MDC` 和 `TTL` 上下文。
2.  **重放 (Replay)**：在子线程开始执行任务前，将捕获的上下文“注入”到子线程中。
3.  **恢复 (Restore)**：任务执行完后，清理子线程的上下文，防止污染复用的线程。

---

## 3. 如何使用？

### 3.1 引入依赖
在你的业务模块 (如 `atlas-auth`, `atlas-system`) 的 `pom.xml` 中引入：

```xml
<dependency>
    <groupId>com.bubua12.atlas</groupId>
    <artifactId>atlas-common-concurrent</artifactId>
    <version>${atlas.version}</version>
</dependency>
```

### 3.2 使用默认全局线程池
模块自动配置了一个名为 `atlasAsyncExecutor` 的线程池。

```java
@Service
@Slf4j
public class MyService {

    @Async("atlasAsyncExecutor") // 指定线程池名称
    public void doSomethingAsync() {
        log.info("这里是异步执行，且能拿到 traceId");
    }
}
```

### 3.3 定义业务专用线程池 (推荐)
为了避免核心业务被边缘业务拖垮，建议为关键业务定义独立线程池。

```java
@Configuration
public class OrderConfig {

    @Bean("orderThreadPool")
    public ThreadPoolTaskExecutor orderThreadPool() {
        // 使用 ThreadPoolBuilder 快速构建，自动具备上下文透传能力
        return ThreadPoolBuilder.build("order-async-", 10, 20, 1000);
    }
}
```

---

## 4. 源码导读

- **`config/AsyncConfig.java`**: 开启 `@EnableAsync` 并配置默认 Bean。
- **`factory/ThreadPoolBuilder.java`**: 统一构建入口，封装了参数配置。
- **`factory/AtlasThreadPoolTaskExecutor.java`**: **核心类**，重写了 `execute/submit`，实现了上下文传递逻辑。
- **`wrapper/ThreadMdcUtil.java`**: 具体的 `Runnable/Callable` 包装工具，负责 MDC 的拷贝与清理。
