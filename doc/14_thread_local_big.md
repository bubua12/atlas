# Q

ThreadLocal里面塞这么大的一个LoginUser会有啥影响或者不好的地方不，比如影响速度，拖慢系统响应啥的？

# A

**完全不用担心。** `LoginUser` 放进 ThreadLocal 对性能几乎零影响。

## 内存开销

看看 `LoginUser` 实际有多大：

| 字段                            | 大小估算                      |
|-------------------------------|---------------------------|
| `userId` (Long)               | 24 bytes                  |
| `username` (String)           | ~50 bytes                 |
| `clientIp` (String)           | ~40 bytes                 |
| `token` (String)              | ~300 bytes (JWT)          |
| `deptId` (Long)               | 24 bytes                  |
| `dataScope` (Integer)         | 16 bytes                  |
| `permissions` (Set\<String\>) | 取决于权限数，假设 50 个权限 ≈ 3-5 KB |

**整个对象撑死 5~10 KB**。哪怕你的 Tomcat 有 200 个线程并发，总计也就 **1~2 MB**，相比 JVM 堆（通常几百 MB 到几 GB）完全可以忽略。

## 访问速度

ThreadLocal 的读写本质上是 **直接访问当前线程对象的一个数组槽位**，时间复杂度 O(1)，和访问局部变量差不多——**纳秒级，比一次
Redis 调用快 1000 倍以上**。

## 真正需要关注的是

**每次请求多一次 Redis 调用**（`redisService.get()`），这才是这个改动的实际开销。不过：

- Redis 本地访问通常 **<1ms**
- 你的 `PreAuthorizeAspect` 在权限校验时**本来就要查一次同样的 Redis key**
- 所以可以优化：在 `UserContextInterceptor` 里取出 `LoginUser` 存入上下文后，`PreAuthorizeAspect` 直接从上下文取，**省掉重复的
  Redis 查询**

## 结论

ThreadLocal 放 `LoginUser` 完全没问题，这是业界标准做法（Spring Security 的 `SecurityContextHolder` 也是把整个
`Authentication` 对象塞进 ThreadLocal）。你的 `afterCompletion` 里已经做了 `clear()`，不会有内存泄漏风险。

---

另外注意到你把 `AutoConfiguration.imports` 改回了 `AtlasDataBaseAutoConfiguration`——确保你已经创建了这个类或者把
`MybatisPlusConfig` 重命名成了这个名字，否则之前的 Bean 注册问题还会存在。