> https://chatgpt.com/share/69a7e3c0-dabc-800b-9f69-4b9ce01c3136

# 1、

JWT（JSON Web Token）是一种：
> 用于在各方之间安全传输声明（claims）的标准格式


它的核心定位是：

* ✅ 一种“自包含”的身份凭证格式标准
* ❌ 不是登录框架
* ❌ 不是加密算法
* ❌ 不是认证系统
* ❌ 也不等于加密

JWT 就是一个：
> 字符串

但这个字符串由三部分组成：
`Header.Payload.Signature`

# 2、

在典型微服务架构中：
> - ✔ Gateway 一定要校验 Token（是否过期、是否有效）
> - ✔ Auth 模块负责签发 Token
> - ✔ Gateway 和 Auth 通常都会引入 JWT 相关能力
> - 业务微服务一般不再重复验签（信任 Gateway）



登录流程

1️⃣ 用户登录

1. Client → Gateway → Auth Service
2. Auth 校验用户名密码
3. Auth 生成 JWT
4. 返回给客户端

2️⃣ 之后访问接口

1. Client 带 JWT → Gateway
2. Gateway 验签
3. Gateway 解析用户信息
4. Gateway 转发给业务服务