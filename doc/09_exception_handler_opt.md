# 1、提出问题
```text
分析我的代码就行，先别修改我的代码，我现在系统登录输入了错误的用户名或者密码，GlaleExceptionHandler触发的是系统异常，而不是BusinessException的业务异常，帮我排查一下是什么原因？我抛出的AuthException是BusinessException的子类啊
```
我已经为你排查了代码，找到了导致这个问题的根本原因。

**问题原因：**
这个问题是因为方法上的 AOP 切面（限流切面）**吞掉了真实的异常类型**，将原本抛出的 `AuthException` 包装成了普通的 `RuntimeException`，导致全局异常处理器无法精准匹配。

**详细排查链路如下：**

1. **准确抛出业务异常**：
   当用户名或密码错误时，[PasswordLoginHandler.java](file:///d:/workspaces/IdeaProjects/atlas/atlas-auth/src/main/java/com/bubua12/atlas/auth/handler/PasswordLoginHandler.java#L41-L43) 中确实正确地抛出了 `AuthException`（它是 `BusinessException` 的子类）。
2. **切面拦截并错误包装**：
   异常向上抛出到达了 [AuthController](file:///d:/workspaces/IdeaProjects/atlas/atlas-auth/src/main/java/com/bubua12/atlas/auth/controller/AuthController.java#L40-L46) 的 `login` 方法。由于该方法使用了 `@RedisLimit` 注解，请求会被限流切面 [RedisLimitAspect.java](file:///d:/workspaces/IdeaProjects/atlas/atlas-common/atlas-common-redis/src/main/java/com/bubua12/atlas/common/redis/aspect/RedisLimitAspect.java#L98-L103) 拦截。在这个切面的放行逻辑中：
   ```java
        try {
            // 放行
            result = pjp.proceed();
        } catch (Throwable e) {
            throw new RuntimeException(e); // <--- 问题出在这里！
        }
   ```
   它捕获了目标方法抛出的 `AuthException`，然后**强制用一个新的 `java.lang.RuntimeException` 把它包裹了起来**抛出。
3. **全局异常匹配降级**：
   当异常最终到达 [GlobalExceptionHandler.java](file:///d:/workspaces/IdeaProjects/atlas/atlas-common/atlas-common-web/src/main/java/com/bubua12/atlas/common/web/handler/GlobalExceptionHandler.java#L21-L25) 时，Spring 获取到的最外层异常类型已经变成了普通的 `RuntimeException`。因为它不再是 `BusinessException` 的实例，所以 `@ExceptionHandler(BusinessException.class)` 无法匹配它，最终只能降级进入到 `@ExceptionHandler(Exception.class)`，从而触发了“系统异常”。

**修改思路（供参考）：**
后续修复时，只需要在 `RedisLimitAspect` 里面判断一下，如果是运行时异常直接原样抛出，保留它原本的异常类型即可：
```java
        try {
            // 放行
            result = pjp.proceed();
        } catch (Throwable e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e; // 保持原有异常类型
            }
            throw new RuntimeException(e);
        }
```

既然你要求先别修改代码，你可以先确认一下这个排查结果，如果需要我帮你进行代码修复的话，可以随时告诉我！


# 2、还是有问题
```text
不是的吧，刚开始没进入限流的时候，也是走系统异常啊
```

你的疑问非常合理，我们来把这两种可能的场景（**“没触发限流限制时”** 和 **“代码里还没加限流切面时”**）拆解清楚，你看看属于哪一种：

### 场景一：你的意思是“没有被限流拦截（正常放行）”
如果你的意思是：请求没有达到限流阈值，是正常往下走的，但依然报了系统异常。

**这恰恰就是刚才说的那个切面导致的！**
看 [RedisLimitAspect.java](file:///d:/workspaces/IdeaProjects/atlas/atlas-common/atlas-common-redis/src/main/java/com/bubua12/atlas/common/redis/aspect/RedisLimitAspect.java#L89-L103) 的逻辑：
1. 如果 **触发了限流** (`count == 0`)，切面直接 `return CommonResult.fail(...)`，**根本不会抛出异常**。
2. 如果 **没触发限流**（放行了），切面会执行 `result = pjp.proceed();` 去调用你的业务代码。
3. 你的业务代码抛出了 `AuthException`，然后直接掉进了切面底部的 `catch (Throwable e)` 里，**被强行包装成了 `RuntimeException(e)`**！

所以，**正是因为“没进入限流限制”，业务代码抛出的异常才会被切面的 catch 块捕获并错误包装**，导致全局异常处理器只认得最外层的 `RuntimeException`。

---

### 场景二：你的意思是“我刚开始根本就还没写/没加 @RedisLimit 注解时”
如果你确认在**完全没有限流切面**介入的情况下，依然走了系统异常，那么代码里还隐藏着另一个**异常吞没（Exception Swallowing）**的问题。

请看你的 [AuthServiceImpl.java](file:///d:/workspaces/IdeaProjects/atlas/atlas-auth/src/main/java/com/bubua12/atlas/auth/service/impl/AuthServiceImpl.java#L95-L101)：
```java
        } catch (Exception e) { // <--- 这里捕获到了你的 AuthException
            // 登录失败，记录失败次数
            if (ip != null && username != null) {
                loginFailRecordService.recordLoginFail(ip, username); // <--- 注意这里！
            }
            throw e;
        }
```
**隐患分析：**
当抛出 `AuthException` 后，会被这里的 `catch` 捕获，接着去执行 `loginFailRecordService.recordLoginFail`。
如果 `recordLoginFail` 方法内部发生了**任何异常**（例如：数据库里 `sys_login_fail_record` 表还不存在导致 SQL 报错、MyBatis-Plus 插入异常等），那么：
**新的数据库异常会直接抛出，把原本的 `AuthException` 彻底覆盖掉！**

由于 SQL 异常（比如 `BadSqlGrammarException`）也是 `RuntimeException`，它不属于 `BusinessException`，最终到了全局异常处理器，就会触发 `@ExceptionHandler(Exception.class)`，返回系统异常。

### 💡 建议的排查步骤
你可以在控制台看看 `GlobalExceptionHandler` 打印的 `log.error("系统异常: {}", e.getMessage(), e);` 详细堆栈：
1. 如果堆栈里看到 `java.lang.RuntimeException: com.bubua12.atlas.auth.exception.AuthException...`，那就是**场景一**（被限流切面包装了）。
2. 如果堆栈里看到 `BadSqlGrammarException` 或其他数据库相关异常，那就是**场景二**（记录登录失败日志时出错了，掩盖了业务异常）。

你可以先核对一下控制台的真实报错堆栈，确认是哪一种情况。如果是场景二，需要先建表或者排查失败记录插入失败的原因；如果是场景一，修改一下那个切面的 catch 逻辑就行。代码按我上次说的调整切面即可。


# 3、找到问题
```java
这个是日志：2026-03-18T09:54:05.471+08:00  INFO 23716 --- [atlas-auth] [nio-9100-exec-1] c.b.a.c.redis.aspect.RedisLimitAspect    : Redis限流注解信息，限流的key: com.bubua12.atlas.auth.controller.AuthController:login:login
2026-03-18T09:54:05.766+08:00 ERROR 23716 --- [atlas-auth] [nio-9100-exec-1] c.b.a.c.w.h.GlobalExceptionHandler       : 系统异常: com.bubua12.atlas.auth.exception.AuthException: 用户名或密码错误

java.lang.RuntimeException: com.bubua12.atlas.auth.exception.AuthException: 用户名或密码错误
	at com.bubua12.atlas.common.redis.aspect.RedisLimitAspect.around(RedisLimitAspect.java:102) ~[classes/:na]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[na:na]
	at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[na:na]
	at org.springframework.aop.aspectj.AbstractAspectJAdvice.invokeAdviceMethodWithGivenArgs(AbstractAspectJAdvice.java:637) ~[spring-aop-6.1.5.jar:6.1.5]
	at org.springframework.aop.aspectj.AbstractAspectJAdvice.invokeAdviceMethod(AbstractAspectJAdvice.java:627) ~[spring-aop-6.1.5.jar:6.1.5]
	at org.springframework.aop.aspectj.AspectJAroundAdvice.invoke(AspectJAroundAdvice.java:71) ~[spring-aop-6.1.5.jar:6.1.5]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:173) ~[spring-aop-6.1.5.jar:6.1.5]
	at org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.proceed(CglibAopProxy.java:765) ~[spring-aop-6.1.5.jar:6.1.5]
	at org.springframework.aop.framework.CglibAopProxy$DynamicAdvisedInterceptor.intercept(CglibAopProxy.java:717) ~[spring-aop-6.1.5.jar:6.1.5]
	at com.bubua12.atlas.auth.controller.AuthController$$SpringCGLIB$$0.login(<generated>) ~[classes/:na]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[na:na]
	at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[na:na]
	at org.springframework.web.method.support.InvocableHandlerMethod.doInvoke(InvocableHandlerMethod.java:255) ~[spring-web-6.1.5.jar:6.1.5]
	at org.springframework.web.method.support.InvocableHandlerMethod.invokeForRequest(InvocableHandlerMethod.java:188) ~[spring-web-6.1.5.jar:6.1.5]
	at org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod.invokeAndHandle(ServletInvocableHandlerMethod.java:118) ~[spring-webmvc-6.1.5.jar:6.1.5]
	at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.invokeHandlerMethod(RequestMappingHandlerAdapter.java:925) ~[spring-webmvc-6.1.5.jar:6.1.5]
	at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.handleInternal(RequestMappingHandlerAdapter.java:830) ~[spring-webmvc-6.1.5.jar:6.1.5]
	at org.springframework.web.servlet.mvc.method.AbstractHandlerMethodAdapter.handle(AbstractHandlerMethodAdapter.java:87) ~[spring-webmvc-6.1.5.jar:6.1.5]
	at org.springframework.web.servlet.DispatcherServlet.doDispatch(DispatcherServlet.java:1089) ~[spring-webmvc-6.1.5.jar:6.1.5]
	at org.springframework.web.servlet.DispatcherServlet.doService(DispatcherServlet.java:979) ~[spring-webmvc-6.1.5.jar:6.1.5]
	at org.springframework.web.servlet.FrameworkServlet.processRequest(FrameworkServlet.java:1014) ~[spring-webmvc-6.1.5.jar:6.1.5]
	at org.springframework.web.servlet.FrameworkServlet.doPost(FrameworkServlet.java:914) ~[spring-webmvc-6.1.5.jar:6.1.5]
	at jakarta.servlet.http.HttpServlet.service(HttpServlet.java:590) ~[tomcat-embed-core-10.1.19.jar:6.0]
	at org.springframework.web.servlet.FrameworkServlet.service(FrameworkServlet.java:885) ~[spring-webmvc-6.1.5.jar:6.1.5]
	at jakarta.servlet.http.HttpServlet.service(HttpServlet.java:658) ~[tomcat-embed-core-10.1.19.jar:6.0]
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:205) ~[tomcat-embed-core-10.1.19.jar:10.1.19]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:149) ~[tomcat-embed-core-10.1.19.jar:10.1.19]
	at org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:51) ~[tomcat-embed-websocket-10.1.19.jar:10.1.19]
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:174) ~[tomcat-embed-core-10.1.19.jar:10.1.19]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:149) ~[tomcat-embed-core-10.1.19.jar:10.1.19]
	at org.springframework.web.filter.RequestContextFilter.doFilterInternal(RequestContextFilter.java:100) ~[spring-web-6.1.5.jar:6.1.5]
	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116) ~[spring-web-6.1.5.jar:6.1.5]
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:174) ~[tomcat-embed-core-10.1.19.jar:10.1.19]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:149) ~[tomcat-embed-core-10.1.19.jar:10.1.19]
	at org.springframework.web.filter.FormContentFilter.doFilterInternal(FormContentFilter.java:93) ~[spring-web-6.1.5.jar:6.1.5]
	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116) ~[spring-web-6.1.5.jar:6.1.5]
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:174) ~[tomcat-embed-core-10.1.19.jar:10.1.19]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:149) ~[tomcat-embed-core-10.1.19.jar:10.1.19]
	at org.springframework.web.filter.ServerHttpObservationFilter.doFilterInternal(ServerHttpObservationFilter.java:109) ~[spring-web-6.1.5.jar:6.1.5]
	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116) ~[spring-web-6.1.5.jar:6.1.5]
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:174) ~[tomcat-embed-core-10.1.19.jar:10.1.19]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:149) ~[tomcat-embed-core-10.1.19.jar:10.1.19]
	at org.springframework.web.filter.CharacterEncodingFilter.doFilterInternal(CharacterEncodingFilter.java:201) ~[spring-web-6.1.5.jar:6.1.5]
	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116) ~[spring-web-6.1.5.jar:6.1.5]
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:174) ~[tomcat-embed-core-10.1.19.jar:10.1.19]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:149) ~[tomcat-embed-core-10.1.19.jar:10.1.19]
	at org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:167) ~[tomcat-embed-core-10.1.19.jar:10.1.19]
	at org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:90) ~[tomcat-embed-core-10.1.19.jar:10.1.19]
	at org.apache.catalina.authenticator.AuthenticatorBase.invoke(AuthenticatorBase.java:482) ~[tomcat-embed-core-10.1.19.jar:10.1.19]
	at org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:115) ~[tomcat-embed-core-10.1.19.jar:10.1.19]
	at org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:93) ~[tomcat-embed-core-10.1.19.jar:10.1.19]
	at org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:74) ~[tomcat-embed-core-10.1.19.jar:10.1.19]
	at org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:344) ~[tomcat-embed-core-10.1.19.jar:10.1.19]
	at org.apache.coyote.http11.Http11Processor.service(Http11Processor.java:391) ~[tomcat-embed-core-10.1.19.jar:10.1.19]
	at org.apache.coyote.AbstractProcessorLight.process(AbstractProcessorLight.java:63) ~[tomcat-embed-core-10.1.19.jar:10.1.19]
	at org.apache.coyote.AbstractProtocol$ConnectionHandler.process(AbstractProtocol.java:896) ~[tomcat-embed-core-10.1.19.jar:10.1.19]
	at org.apache.tomcat.util.net.NioEndpoint$SocketProcessor.doRun(NioEndpoint.java:1744) ~[tomcat-embed-core-10.1.19.jar:10.1.19]
	at org.apache.tomcat.util.net.SocketProcessorBase.run(SocketProcessorBase.java:52) ~[tomcat-embed-core-10.1.19.jar:10.1.19]
	at org.apache.tomcat.util.threads.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1191) ~[tomcat-embed-core-10.1.19.jar:10.1.19]
	at org.apache.tomcat.util.threads.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:659) ~[tomcat-embed-core-10.1.19.jar:10.1.19]
	at org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:63) ~[tomcat-embed-core-10.1.19.jar:10.1.19]
	at java.base/java.lang.Thread.run(Thread.java:1583) ~[na:na]
Caused by: com.bubua12.atlas.auth.exception.AuthException: 用户名或密码错误
	at com.bubua12.atlas.auth.handler.PasswordLoginHandler.authenticate(PasswordLoginHandler.java:42) ~[classes/:na]
	at com.bubua12.atlas.auth.service.impl.AuthServiceImpl.login(AuthServiceImpl.java:75) ~[classes/:na]
	at com.bubua12.atlas.auth.controller.AuthController.login(AuthController.java:44) ~[classes/:na]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[na:na]
	at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[na:na]
	at org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection(AopUtils.java:351) ~[spring-aop-6.1.5.jar:6.1.5]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.invokeJoinpoint(ReflectiveMethodInvocation.java:196) ~[spring-aop-6.1.5.jar:6.1.5]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:163) ~[spring-aop-6.1.5.jar:6.1.5]
	at org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.proceed(CglibAopProxy.java:765) ~[spring-aop-6.1.5.jar:6.1.5]
	at org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint.proceed(MethodInvocationProceedingJoinPoint.java:89) ~[spring-aop-6.1.5.jar:6.1.5]
	at com.bubua12.atlas.common.log.aspect.OperLogAspect.around(OperLogAspect.java:86) ~[classes/:na]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[na:na]
	at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[na:na]
	at org.springframework.aop.aspectj.AbstractAspectJAdvice.invokeAdviceMethodWithGivenArgs(AbstractAspectJAdvice.java:637) ~[spring-aop-6.1.5.jar:6.1.5]
	at org.springframework.aop.aspectj.AbstractAspectJAdvice.invokeAdviceMethod(AbstractAspectJAdvice.java:627) ~[spring-aop-6.1.5.jar:6.1.5]
	at org.springframework.aop.aspectj.AspectJAroundAdvice.invoke(AspectJAroundAdvice.java:71) ~[spring-aop-6.1.5.jar:6.1.5]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:173) ~[spring-aop-6.1.5.jar:6.1.5]
	at org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.proceed(CglibAopProxy.java:765) ~[spring-aop-6.1.5.jar:6.1.5]
	at org.springframework.aop.interceptor.ExposeInvocationInterceptor.invoke(ExposeInvocationInterceptor.java:97) ~[spring-aop-6.1.5.jar:6.1.5]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:184) ~[spring-aop-6.1.5.jar:6.1.5]
	at org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.proceed(CglibAopProxy.java:765) ~[spring-aop-6.1.5.jar:6.1.5]
	at org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint.proceed(MethodInvocationProceedingJoinPoint.java:89) ~[spring-aop-6.1.5.jar:6.1.5]
	at com.bubua12.atlas.common.redis.aspect.RedisLimitAspect.around(RedisLimitAspect.java:100) ~[classes/:na]
	... 61 common frames omitted
```
这份日志非常完美地印证了我们刚才分析的**场景一**！

你仔细看日志里打印的这几行关键信息：

1. **进入了限流切面（未被拦截，正常放行）**：
   `INFO ... Redis限流注解信息，限流的key: ...`
   这说明请求确实走进了 `@RedisLimit` 切面，并且没有触发限流（如果触发限流就直接 return fail 了），而是继续往下调用了。

2. **异常被包装成了 RuntimeException**：
   ```text
   java.lang.RuntimeException: com.bubua12.atlas.auth.exception.AuthException: 用户名或密码错误 
   	 at com.bubua12.atlas.common.redis.aspect.RedisLimitAspect.around(RedisLimitAspect.java:102) 
   ```
   看这里，最外层的异常变成了 `java.lang.RuntimeException`，并且明确指出了是 [RedisLimitAspect.java:102](file:///d:/workspaces/IdeaProjects/atlas/atlas-common/atlas-common-redis/src/main/java/com/bubua12/atlas/common/redis/aspect/RedisLimitAspect.java#L102) 这行代码抛出的。

3. **真实的业务异常变成了“内部原因 (Caused by)”**：
   ```text
   Caused by: com.bubua12.atlas.auth.exception.AuthException: 用户名或密码错误 
   	 at com.bubua12.atlas.auth.handler.PasswordLoginHandler.authenticate(PasswordLoginHandler.java:42) 
   ```
   你抛出的 `AuthException` 变成了 `Caused by`。

### 总结发生的过程：
1. 你的 `PasswordLoginHandler` 正确抛出了 `AuthException`。
2. 异常向外抛，经过了操作日志切面 `OperLogAspect`，它内部是 `catch (Throwable e) { ... throw e; }`，直接原样抛出了，没问题。
3. 异常继续向外抛，到了限流切面 `RedisLimitAspect`，它执行了：
   ```java
   catch (Throwable e) {
       throw new RuntimeException(e); // 第102行：在这里，你的 AuthException 被包进了一个新的 RuntimeException 里！
   }
   ```
4. 到了 `GlobalExceptionHandler` 时，它拿到的是外面这层包装的 `RuntimeException`。因为 `RuntimeException` 并不是 `BusinessException`（反过来才是），所以 `@ExceptionHandler(BusinessException.class)` 匹配不上。
5. 最终只能走到 `@ExceptionHandler(Exception.class)`，于是控制台打印出了“系统异常”。

**破案了**：就是 `RedisLimitAspect.java` 第102行这句 `throw new RuntimeException(e);` 搞的鬼。只要把那里改成把原来的业务异常原样抛出，你的全局异常处理就能精准捕获就能恢复正常了！


# 4、问题处理方案
好的，你需要修改的文件是 [RedisLimitAspect.java](file:///d:/workspaces/IdeaProjects/atlas/atlas-common/atlas-common-redis/src/main/java/com/bubua12/atlas/common/redis/aspect/RedisLimitAspect.java#L98-L103)。

将第 98 行到 103 行的 `try-catch` 代码块：

```java
        try {
            // 放行
            result = pjp.proceed();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
```

修改为如下形式（保留原本的异常类型抛出）：

```java
        try {
            // 放行
            result = pjp.proceed();
        } catch (Throwable e) {
            if (e instanceof RuntimeException) {
                // 如果是运行时异常（包含你自定义的 AuthException / BusinessException），直接原样抛出
                throw (RuntimeException) e;
            }
            // 对于非运行时异常（比如 IOException 等受检异常），再进行包装
            throw new RuntimeException(e);
        }
```

**修改原理解释：**
你的 `AuthException` 继承自 `BusinessException`，而 `BusinessException` 继承自 `RuntimeException`。通过加上 `e instanceof RuntimeException` 的判断，切面遇到业务异常时就会直接 `throw (RuntimeException) e;` 原样抛出。这样到了全局异常处理器（GlobalExceptionHandler）那里，接收到的就依然是你抛出的那个原汁原味的 `AuthException`，从而能被精准捕获为业务异常了。


# 5、疑问
```text
我有点不太懂了，这俩不都是RuntimeException么，为啥还要e instanceof RuntimeException呢
```

这个问题问得很棒！之所以要加 `e instanceof RuntimeException` 判断，原因在于 `pjp.proceed()` 抛出的异常类型是 **`Throwable`**，而不仅仅是 `RuntimeException`。

我们来看 `pjp.proceed()` 的签名：
```java
public Object proceed() throws Throwable;
```

**这里有两类异常：**

1.  **受检异常 (Checked Exception)**：比如 `IOException`、`SQLException` 等。这类异常 **不是** `RuntimeException` 的子类。
2.  **运行时异常 (RuntimeException)**：比如 `NullPointerException`、以及你自己定义的 `BusinessException` / `AuthException`。这类异常 **是** `RuntimeException` 的子类。

---

### 如果直接写 `throw (RuntimeException) e`
如果 `pjp.proceed()` 抛出的恰好是一个受检异常（比如 `IOException`），那么 `e` 就不是 `RuntimeException`。
这时候你强行转型 `(RuntimeException) e`，就会在运行时直接报错：**`ClassCastException`**（类型转换异常），因为你试图把一个 `IOException` 强转成 `RuntimeException`。

### 如果写成 `throw new RuntimeException(e)` (你原来的写法)
这就相当于把 **所有异常**（不管是业务异常，还是受检异常）统统打包塞进了一个 **新的** `RuntimeException` 壳子里。
*   **后果**：原本是 `AuthException`，被包了一层后，外面拿到的就是 `RuntimeException` 了。`GlobalExceptionHandler` 去剥开它之前，只认得它是个 `RuntimeException`，所以就不会进 `BusinessException` 的处理逻辑了。

### 为什么要 `if (e instanceof RuntimeException)`
这个判断的作用就是 **“分类讨论”**：

1.  **如果是运行时异常（比如你的 `AuthException`）**：
    *   直接强转 `(RuntimeException) e` 并抛出。
    *   **效果**：抛出去的还是原来的那个 `AuthException` 对象，没变味。
2.  **如果是受检异常（比如 `IOException`）**：
    *   因为不能直接抛出 `Throwable`（除非你的方法签名上也声明了 `throws Throwable`），所以必须把它包装一下。
    *   **效果**：走下面的 `throw new RuntimeException(e)`，把它包起来抛出。

**总结一下：**
*   **你的 `AuthException`**：是 `RuntimeException` -> 走进 `if` -> 原样抛出 -> 全局异常处理器能认出来。
*   **其他 `IOException`**：不是 `RuntimeException` -> 走 `else` -> 包装成 `RuntimeException` 抛出 -> 变成系统异常。

所以这个 `if` 是为了保全你的业务异常“原汁原味”地抛出去，同时又能兼容处理那些受检异常。