1、网关签名的作用
让下游服务相信：这个请求确实是网关放行过的，而且用户身份信息是网关签出来的，不是客户端自己伪造的。
这里就是用签名来证明的

- methodName
    - 请求方法，比如 GET、POST
- forwardedPath
    - 下游服务最终真正收到的路径
- payload
    - 当前登录用户信息，序列化后再 Base64 编码
- timestamp
    - 当前时间戳
      这些值会一起参与签名。


2. 为什么要签这 4 个值

```java
String signature = requestSignatureService.signGatewayRequest(methodName, forwardedPath, payload, timestamp);
```

签名本质上是：

- 网关拿一个只有自己和下游知道的 secret
- 对这几个字段做 HMAC
- 生成一个签名字符串

下游收到后会用同样规则再算一遍：

- 如果结果一样，说明内容没被改
- 如果结果不一样，说明被篡改了，直接拒绝

所以这几个字段分别在保护什么：
payload：保护“用户身份内容不能被改”

比如客户端想伪造：
- userId=1
- username=admin
- permissions=::*

如果没有签名，客户端只要自己塞请求头就行。
有签名后，客户端不知道 secret，就没法造出合法签名。

  ---
timestamp：保护“请求不是无限期可复用”
现在没有 nonce 了，所以它不是严格防重放，只是：

- 超过时间窗口就无效
- 减少旧请求被长期重复使用

  ---
method + path： 保护“签名不能脱离原始请求上下文复用”
比如本来网关签的是：
- POST /system/user/create

攻击者把同样的 payload 和 signature 拿去打：
- POST /system/user/delete

如果 path 也参与签名，下游重算后就不一样，验签失败。

所以它的作用是防止：
- 换接口
- 换方法
- 把某个合法签名挪去别的接口复用


6. 下游为什么能信这几个头

因为下游不会“直接信头”，而是会重新验签。
对应逻辑在：

- atlas-common/atlas-common-web/src/main/java/com/bubua12/atlas/common/web/interceptor/GatewayIdentityInterceptor.java
- atlas-common/atlas-common-security/src/main/java/com/bubua12/atlas/common/security/service/RequestSignatureService.j
  ava

下游大致会做：

1. 取出
   - X_LOGIN_USER
   - X_GATEWAY_TIMESTAMP
   - X_GATEWAY_SIGNATURE
2. 用自己本地同一个 secret
3. 按同样规则重算：
   - method
   - path
   - payload
   - timestamp
4. 比较签名是否一致
5. 一致才把 payload 解析成用户上下文



这套机制到底防什么

它主要防这几类问题。

1. 防客户端伪造用户身份

客户端不能自己构造：

- X_LOGIN_USER=admin
- X_GATEWAY_SIGNATURE=...

因为没有 secret，签不出来。

  ---
2. 防中途篡改用户信息

即使有人把请求头里的用户信息改了：

- userId 改掉
- deptId 改掉
- permissions 改掉

下游一验签就会失败。

  ---
3. 防把一个接口的签名拿去另一个接口复用

因为 method/path 参与签名。

  ---
4. 限制旧请求长期复用

因为 timestamp 参与签名并且会校验时间窗口。


---
## 执行的先后顺序

一、先给结论：大顺序是这样

请求进入服务后，整体顺序是：

1. 先执行 MVC 拦截器 preHandle

先后是：
- GatewayIdentityInterceptor
- UserContextInterceptor

2. 再进入 Controller 方法调用链

但这时候如果 Controller 方法上有 AOP 注解，实际会先进入切面：

- InternalApiAspect
- PreAuthorizeAspect
- 最后才真正执行 Controller 方法体

3. Controller 方法执行完后

再走拦截器的后续阶段：
- postHandle（如果有）
- afterCompletion

你这里 UserContextInterceptor.afterCompletion() 还会清理 SecurityContextHolder