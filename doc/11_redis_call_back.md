# 关于Redis的管道对多条命令封装以减少网络流量的实践

## 1.1 先看一段代码

```java
// 使用 executePipelined，返回值是一个 List，里面按顺序装着那 5 个命令的结果
List<Object> list = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
    
    // 命令 1：检查“账号”是否被锁（返回 Boolean）
    connection.exists("auth:account_locked_expire:admin".getBytes());
    
    // 命令 2：检查“手机号”是否被锁（返回 Boolean）
    connection.exists("auth:phone_locked_expire:138xxxx".getBytes());
    
    // 命令 3：检查“IP”是否被锁（返回 Boolean）
    connection.exists("auth:ip_locked_expire:192.168.1.1".getBytes());
    
    // 命令 4：获取系统配置的“最大在线人数”（返回 String）
    connection.get("auth:threshold_login_online".getBytes());
    
    // 命令 5：获取这个账号“当前登录了多少个终端”（返回 Long）
    connection.hLen("auth:user_to_access:admin".getBytes());

    // 必须返回 null，因为真正的结果会被 Spring 拦截并塞进外面的 list 里
    return null; 
}, redisTemplate.getValueSerializer());

// 解析结果：因为放进去是 5 个命令，所以回来的 list 长度必定是 5，且顺序一一对应！
boolean isAccountLocked = (Boolean) list.get(0);
boolean isPhoneLocked = (Boolean) list.get(1);
boolean isIpLocked = (Boolean) list.get(2);   // ⬅️ 这里就是你之前看到的 userInfo.getIpLocked() 的数据来源！
String maxOnline = (String) list.get(3);
Long currentOnline = (Long) list.get(4);

// 最后把这些查到的结果塞进 userInfo 对象里，供后面的 LoginUtil 使用
loginUserInfo.setHasLocked(isAccountLocked);
loginUserInfo.setIpLocked(isIpLocked);
```

## 1.2 什么是管道？

## 1.2.1 关于管道在代码中的应用

### **1. 哪里设置了 userInfo.ipLocked**

`userInfo.setIpLocked`
是在 [PfLoginUserServiceImpl.java](file:///d:/workspaces/IdeaProjects/empoworx-platform/src/backend/syncplant-system-pf/src/main/java/com/springsciyon/system/pf/service/impl/PfLoginUserServiceImpl.java#L141)
的 `getLoginUserInfo` 方法中设置的。

- **具体逻辑**：
    - 该方法使用 Redis **流水线 (Pipelined)** 技术同时查询了多个缓存项，以减少网络往返延迟。
    - 其中第三个查询（索引为 2）是检查是否存在 IP 锁定键：`CacheNames.IPEXPIRE_KEY + WebUtil.getIP()`。
    - 代码片段：
      ```java
      // PfLoginUserServiceImpl.java 第 141 行
      loginUserInfo.setIpLocked(optionalIpexpire.isPresent() && (Boolean) optionalIpexpire.get());
      ```

### **2. 为什么在 LoginUtil.checkAccount 中使用它，而不是直接查 Redis**

在 [LoginUtil.java](file:///d:/workspaces/IdeaProjects/empoworx-platform/src/backend/syncplant-frame-auth/src/main/java/com/springsciyon/system/auth/login/utils/LoginUtil.java#L143)
中，通过 `userInfo.getIpLocked()` 进行判断，其核心原因如下：

- **性能优化 (Redis Pipelining)**：
  `PfLoginUserServiceImpl.getLoginUserInfo` 通过一次 Redis 批量查询（Pipelined）获取了用户状态的所有关键信息，包括：
    1. 账号是否被锁定 (`hasLocked`)
    2. IP 是否被锁定 (`ipLocked`)
    3. 系统在线人数阈值
    4. 当前同账号在线人数

  如果在 `LoginUtil.checkAccount` 中逐个直接查询 Redis，会产生多次独立的 Redis 网络请求，增加登录过程的耗时。通过预先在
  `getLoginUserInfo` 中一次性抓取并封装进 `userInfo` 对象，可以显著提高响应速度。

- **职责分离与微服务解耦**：
  `LoginUtil` 调用 `getUserByCode`（实际上是远程调用 `loginUserService.getLoginUserInfo`），将复杂的用户状态校验逻辑封装在服务端。
  `LoginUtil` 作为一个工具类，只需根据返回的 `userInfo` 状态进行逻辑判断（如抛出异常），而不需要关心底层是如何查询这些状态的。

- **TTL 查询的时机**：
  只有当 `userInfo.getIpLocked()` 为 `true` 时，`LoginUtil` 才会去查询具体的 TTL（剩余锁定时间），用于向前端返回友好的错误提示。这避免了在正常登录情况下不必要的
  TTL 查询开销。

这是一个非常敏锐的问题！“既然我要判断 IP 有没有被锁，直接去 Redis 里 `hasKey` 查一下不就完事了吗？为什么要先在别的地方查好，塞进
`userInfo.getIpLocked()` 里，然后在这里 `if (userInfo.getIpLocked())` 绕个圈子呢？”

答案是：**为了极致的性能优化（Redis Pipelining）和减少网络 IO 延迟。**

你的项目在这里使用了一种非常高级的性能优化手段。我来给你详细解释一下这背后的巧思：

### 1. 登录时需要查多少次 Redis？

当一个用户点击登录时，系统为了保证安全和策略生效，**不止需要查 IP 有没有被锁**，它还需要查很多东西：

1. **查账号是否被锁**（`auth:account_locked_expire:admin`）
2. **查 IP 是否被锁**（`auth:ip_locked_expire:192.168.1.1`）
3. **查当前系统总在线人数**（为了限制系统最大并发人数）
4. **查当前账号是否已经在别的地方登录了**（为了实现踢人下线或禁止多点登录）

### 2. 如果直接写代码会发生什么？（慢）

如果你在 `LoginUtil.checkAccount` 里面直接一行行写：

```java
if (redis.hasKey("auth:ip_locked_expire:" + ip)) { ... }
if (redis.hasKey("auth:account_locked_expire:" + account)) { ... }
int onlineCount = redis.get("auth:online_count");
// ...
```

这段代码看起来很直观，但它会产生 **4 次独立的 Redis 网络请求**。
每一次请求，都要经历：`Java 发送命令 -> 网络传输 -> Redis 处理 -> 网络传回结果 -> Java 接收`。
在微服务或高并发场景下，这种**频繁的、往返的（Round-Trip）小包网络通信**是非常消耗时间的。

### 3. 你项目里的神仙做法（快：Redis Pipelining）

为了解决这个问题，你的项目在加载用户信息（查数据库获取用户基本信息）的那一刻，做了一件很聪明的事。

在 [PfLoginUserServiceImpl.java](file:///d:/workspaces/IdeaProjects/empoworx-platform/src/backend/syncplant-system-pf/src/main/java/com/springsciyon/system/pf/service/impl/PfLoginUserServiceImpl.java)
的 `getLoginUserInfo` 方法里，它使用了 **Redis 管道（Pipelining）技术**。

```java
// 伪代码，展示你项目底层的流水线技术
List<Object> results = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
    // 1. 把“查账号锁定”命令丢进管道
    connection.exists("auth:account_locked_expire:...");
    // 2. 把“查IP锁定”命令丢进管道
    connection.exists("auth:ip_locked_expire:...");
    // 3. 把“查在线人数”命令丢进管道
    connection.get("...");
    return null;
});

// Redis 把上面 3 个命令打包，一次性执行完，把结果打包成一个 List 扔回来！
loginUserInfo.setHasLocked((Boolean) results.get(0));
loginUserInfo.setIpLocked((Boolean) results.get(1)); // ⬅️ 你的 getIpLocked 就是在这里被赋值的！
// ...
```

**管道技术的优势：**
它把本来需要跑 4 趟的 Redis 请求，**打包成 1 个大包裹**，只发 1 次网络请求给 Redis，Redis 算完后把 4
个结果打包一次性发回来。这极大地降低了网络延迟。

### 4. 为什么要多此一举去查 `ttl`？

这就解释了你看到的这段代码：

```java
if (userInfo.getIpLocked()) { 
     long ttl = syncplantRedis.ttl(...); 
     throw new ServiceException("剩余锁定时间: " + ttl); 
}
```

* **因为绝大多数人是不会被锁的！** 99% 的正常登录，`getIpLocked()` 都是 `false`。
* 所以，系统在打包查询（Pipeline）的时候，**只查了 `exists`（存不存在），没有查 `ttl`（剩多少秒）**，因为查 `ttl` 比较费性能，且对
  99% 的人没用。
* 只有当那 1% 的倒霉蛋真的被锁了（`if` 成立），系统为了给前端一个友好的提示语（“请在 X 秒后再试”），才会**按需（Lazy Load）**再去
  Redis 单独查一次 `ttl`。

### 总结

你看到的 `if (userInfo.getIpLocked())` 不是脱裤子放屁，而是：

1. **为了极致性能**：提前把所有的 Redis 检查项打包成 1 次请求查完了，塞到了 `userInfo` 对象里。
2. **按需查询倒计时**：只有确认你被锁了，为了给你展示倒计时，才会再单独去 Redis 查一次 `ttl`。

## 管道技术的简介

在 `empoworx-platform` 项目中，`executePipelined` 主要用于优化 Redis 操作的性能。通过将多个 Redis
命令打包在一次网络往返中执行，可以显著减少网络延迟对登录等关键业务流程的影响。

以下是基于 `PfLoginUserServiceImpl` 实际代码的详细分析：

### **核心用途：Redis 管道技术 (Pipelining)**

`executePipelined` 是 Spring Data Redis 提供的一个高级功能。它允许开发者在一个 `RedisCallback` 中连续调用多个 Redis
命令，而不需要等待每个命令的即时响应。所有命令会被一次性发送到 Redis 服务器，服务器处理完后一次性返回所有结果。

### **在 PfLoginUserServiceImpl 中的具体应用**

在登录流程的 `getLoginUserInfo` 方法中，系统需要频繁从 Redis 中获取多项状态信息。

#### **场景 1：系统管理模块 (System-PF)**

在 [PfLoginUserServiceImpl.java](file:///d:/workspaces/IdeaProjects/empoworx-platform/src/backend/syncplant-system-pf/src/main/java/com/springsciyon/system/pf/service/impl/PfLoginUserServiceImpl.java#L113-131)
中，一次性执行了 5 个查询操作：

- **账号锁定状态**：检查账号是否因错误次数过多被锁定。
- **手机/邮箱锁定**：检查特定的登录介质是否被锁定。
- **IP 锁定**：检查当前客户端 IP 是否被临时封禁。
- **在线人数阈值**：获取系统配置的最大同时在线人数。
- **当前在线人数**：获取该账号当前已登录的会话数量。

```java
List<Object> list = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
    // 1. 检查账号锁定 (exists)
    connection.exists(raw(keySerializer, CacheNames.EXPIRE_KEY + account));
    // 2. 检查手机/邮箱锁定 (exists)
    connection.exists(raw(keySerializer, CacheNames.EPPEXPIRE_KEY + account));
    // 3. 检查 IP 锁定 (exists)
    connection.exists(raw(keySerializer, CacheNames.IPEXPIRE_KEY + WebUtil.getIP()));
    // 4. 获取在线阈值 (get)
    connection.get(raw(keySerializer, CacheNames.THRESHOLD_LOGIN_ONLINE));
    // 5. 获取当前在线人数 (hLen)
    connection.hLen(raw(keySerializer, AuthConstant.REDIS_UNAME_TO_ACCESS + ...));
    return null;
}, redisTemplate.getValueSerializer());
```

#### **场景 2：权限认证模块 (Frame-Auth)**

在 [PfLoginUserServiceImpl.java](file:///d:/workspaces/IdeaProjects/empoworx-platform/src/backend/syncplant-frame-auth/src/main/java/com/springsciyon/system/auth/login/service/PfLoginUserServiceImpl.java#L83-108)
中，除了查询，还包含了 **写操作**：

- **查询**：账号锁定、IP 锁定、当前在线人数。
- **更新**：将查询到的最新用户信息（`USER_INFO_CACHE`）和组织权限（`USER_ORG_PERMISSION_CACHE`）异步写入缓存。

### **技术优势**

- **降低延迟**：登录是用户体验的第一步。如果不使用 Pipelining，5 次查询意味着 5 次网络请求（RTT），在高延迟网络环境下会产生明显的卡顿感。
- **原子性感知**：虽然 Redis Pipeline 不具备数据库事务的原子性（中途某个命令失败不会回滚其他命令），但在业务逻辑上，它保证了这一组状态是在极短的时间片内被获取或更新的。
- **结果解析**：`executePipelined` 返回一个 `List<Object>`，其元素的顺序严格对应代码中调用命令的顺序。开发者可以通过
  `list.get(0)`、`list.get(1)` 等方式依次获取并转换结果。

### **总结**

项目中对 `executePipelined` 的使用非常规范，主要集中在 **高频、多步骤** 的缓存操作场景（如登录信息校验和用户信息预热），通过减少网络
I/O 开销来提升系统的并发处理能力和响应速度。

---
**任务完成情况**

- [x] 搜索并定位 `executePipelined` 在代码中的使用。
- [x] 重点分析 `PfLoginUserServiceImpl` 的两个版本实现。
- [x] 解释了 Pipelining 的性能优势和结果处理方式。

`redisTemplate.executePipelined` 是 Spring Data Redis 提供的一个极其强大的功能，它底层使用的是 Redis 的 *
*管道技术（Pipelining）**。

为了让你彻底明白，我们从“是什么”、“怎么用”、“为什么用”三个维度来拆解，并结合你项目里真实的登录代码（[PfLoginUserServiceImpl.java](file:///d:/workspaces/IdeaProjects/empoworx-platform/src/backend/syncplant-system-pf/src/main/java/com/springsciyon/system/pf/service/impl/PfLoginUserServiceImpl.java)
）来说明。

---

### 1. 是什么？（打个比方）

假设你要去超市（Redis）买 5 样东西。

* **普通模式（不使用 Pipeline）**：你从家出发，去超市买了一瓶水，回家；然后再从家出发，去超市买了一包盐，回家…… 这样跑 5
  趟，累死你，大部分时间全花在**路上（网络传输）**了。
* **管道模式（使用 Pipeline）**：你拿了一张购物清单，去超市一次性把 5 样东西全扔进购物车，结账后**一次性带回家**。

在 Redis 的世界里，客户端发送一条命令到服务器，服务器处理完再把结果返回给客户端，这个过程叫做一个 **RTT（Round Trip
Time，往返时间）**。
`executePipelined` 就是把多条 Redis 命令打包，通过**一次网络 RTT** 发送给 Redis 服务器，Redis 依次执行完毕后，再把所有结果打包，通过一次网络
RTT 返回给你的 Java 程序。

---

### 2. 怎么用的？（结合你项目的代码）

在你的项目里，当用户登录时，系统需要去 Redis 查 5 个不同的状态来判断能不能放行。代码是这样写的：

```java
// 使用 executePipelined，返回值是一个 List，里面按顺序装着那 5 个命令的结果
List<Object> list = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
    
    // 命令 1：检查“账号”是否被锁（返回 Boolean）
    connection.exists("auth:account_locked_expire:admin".getBytes());
    
    // 命令 2：检查“手机号”是否被锁（返回 Boolean）
    connection.exists("auth:phone_locked_expire:138xxxx".getBytes());
    
    // 命令 3：检查“IP”是否被锁（返回 Boolean）
    connection.exists("auth:ip_locked_expire:192.168.1.1".getBytes());
    
    // 命令 4：获取系统配置的“最大在线人数”（返回 String）
    connection.get("auth:threshold_login_online".getBytes());
    
    // 命令 5：获取这个账号“当前登录了多少个终端”（返回 Long）
    connection.hLen("auth:user_to_access:admin".getBytes());

    // 必须返回 null，因为真正的结果会被 Spring 拦截并塞进外面的 list 里
    return null; 
}, redisTemplate.getValueSerializer());

// 解析结果：因为放进去是 5 个命令，所以回来的 list 长度必定是 5，且顺序一一对应！
boolean isAccountLocked = (Boolean) list.get(0);
boolean isPhoneLocked = (Boolean) list.get(1);
boolean isIpLocked = (Boolean) list.get(2);   // ⬅️ 这里就是你之前看到的 userInfo.getIpLocked() 的数据来源！
String maxOnline = (String) list.get(3);
Long currentOnline = (Long) list.get(4);

// 最后把这些查到的结果塞进 userInfo 对象里，供后面的 LoginUtil 使用
loginUserInfo.setHasLocked(isAccountLocked);
loginUserInfo.setIpLocked(isIpLocked);
```

**用法要点总结**：

1. 把你想执行的所有 Redis 命令，全部写在 `connection.xxx` 里面。
2. 注意：在回调函数里，`connection.get()` 之类的方法**不会立刻返回真实数据**（所以不要在回调里用 `if` 判断结果），它只负责把命令塞进“管道”。
3. 最终返回的 `List<Object>` 里的元素顺序，和你塞入命令的顺序是绝对一致的。

---

### 3. 为啥项目里要用这个东西？

原因只有两个字：**性能**。

在微服务架构或分布式系统中，Java 服务器和 Redis 服务器通常不在同一台机器上。
假设一次网络传输（Java -> Redis -> Java）需要 2 毫秒（这算很快了）。

* **不使用 Pipelined**：你需要跑 5 次网络通信。总网络耗时 = `5 * 2ms = 10ms`。
* **使用 Pipelined**：只跑 1 次网络通信。总网络耗时 = `1 * 2ms = 2ms`。

**别小看这 8 毫秒的差距：**

1. **登录是系统并发最高的接口之一**：如果在早高峰，1000 个人同时登录，不使用管道会产生 5000 次 Redis 网络请求，Redis
   的网卡带宽和连接池可能会瞬间被打满；使用管道，瞬间降到了 1000 次请求。
2. **避免连接池耗尽**：每次请求都要从 Redis 连接池借一个连接，用完归还。跑 5 次就意味着要借/还 5
   次（或者占用连接更长时间）。管道技术能让单个连接的利用率达到最高。

**一句话总结**：
你项目里的架构师非常注重高并发下的性能。他利用 `executePipelined`，把登录时极其繁琐的各种状态校验（账号锁、IP锁、在线人数等），压缩成了一次极其高效的
Redis 交互，既保证了系统的安全性（啥都查了），又保证了用户的登录体验（秒进系统毫无卡顿感降到最低。

# 关于这个RedisCallBack的解释

在对项目代码及配置进行核查后，现将 **Spring Data Redis** 版本情况及 `RedisConnection` 命令（如 `exists`）弃用的原因说明如下：

**项目版本信息**

- **Spring Boot 版本**：[pom.xml](file:///d:/workspaces/IdeaProjects/empoworx-platform/src/backend/pom.xml#L30) 中定义为
  `2.7.18`。
- **Spring Data Redis 版本**：根据 Spring Boot 2.7.18 的依赖管理，项目实际使用的 Spring Data Redis 版本为 **2.7.18**。

**弃用原因分析**
在 Spring Data Redis 2.0 及后续版本中，`RedisConnection` 接口中的许多直接命令方法（如 `exists`, `del`, `type`
等）被标记为弃用，建议通过 `keyCommands()`、`stringCommands()` 等子接口调用。主要原因包括：

- **职责划分与解耦**：Redis 命令数量庞大且持续增加。为了避免 `RedisConnection` 接口过于臃肿，Spring Data 团队将命令按功能分类（如
  Key、String、List、Set、Hash 等），通过专门的命令接口（如 `RedisKeyCommands`）进行管理。
- **API 一致性**：这种结构化的设计使得同步、异步及响应式（Reactive）连接的 API 更加统一，便于在不同驱动（如 Lettuce,
  Jedis）之间切换。
- **更好的扩展性**：随着 Redis 版本的演进（如增加 Stream、JSON 等新模块），通过子接口扩展新命令比直接修改核心连接接口更加灵活且符合开闭原则。

**代码现状与建议**

- **当前用法**
  ：项目中多处（如 [PfLoginUserServiceImpl.java:L87](file:///d:/workspaces/IdeaProjects/empoworx-platform/src/backend/syncplant-frame-auth/src/main/java/com/springsciyon/system/auth/login/service/PfLoginUserServiceImpl.java#L87)
  ）仍在使用弃用的 `connection.exists(...)`。
- **替换方式**：建议将 `connection.exists(key)` 替换为 `connection.keyCommands().exists(key)`。

**总结**
弃用 `RedisConnection` 的直接命令是为了提供更清晰、更具组织性的 API 结构，这是 Spring Data Redis 2.x 及 3.x
版本演进的重要设计方向。在当前 `2.7.18` 版本下，虽然旧方法仍可使用，但建议逐步迁移到 `keyCommands()` 以保证代码的未来兼容性。

这两个问题问得非常深入！这涉及到了 Spring 框架的底层设计模式以及版本演进的历史。我用大白话分两部分给你解释清楚。

### 问题 1：`RedisCallback` 是个啥？为啥要这样写？

这其实是 Spring 框架里极其经典的一种设计模式，叫 **模板方法模式（Template Method Pattern）与回调（Callback）机制**。

**为什么不能直接像平时那样 `redisTemplate.exists(...)`？**

平时我们用 `redisTemplate.opsForValue().get()` 时，底层 Spring 帮你做了很多“脏活累活”：

1. **借连接**：从 Redis 连接池（比如 Lettuce 或 Jedis）里借出一个连接。
2. **执行命令**：发送 `GET` 命令。
3. **处理异常**：如果网络断了，帮你捕捉异常并转换成 Spring 统一的异常。
4. **还连接**：把连接放回连接池，**防止连接泄露（内存溢出）**。

但是！现在我们要用 **管道（Pipeline）** 技术，这意味着：
我们要**借出 1 个连接，拿着这个连接连续执行 5 个命令，最后再还回去**。

**`RedisCallback` 的作用就是把“借”和“还”包起来，只让你写“中间执行”的部分：**

1. `redisTemplate.executePipelined` 就像一个大管家，他说：“连接我帮你借好了，异常我帮你兜底，最后我还帮你还连接。喏，这个
   `connection` 就是借出来的连接，你在这个花括号里（这就是 Callback 回调函数）尽情地写你要执行的命令吧！”
2. 于是你就在 `(connection) -> { ... }` 里面，拿着这**同一个连接**，咔咔一顿塞命令。
3. 大管家看你写完（`return null;`）了，就帮你把这批命令打包发给 Redis，拿到结果后，再安全地把连接还给池子。

如果不用这种 Callback 写法，你的代码就会变成又臭又长的 `try-catch-finally`，而且极容易因为忘记 `close()` 导致服务器崩溃。

---

### 问题 2：为什么 `connection.exists` 会过时？提示用 `keyCommands()` 又是为啥？

这和你项目使用的 **Spring Boot 版本（2.7.x）** 以及底层的 **Spring Data Redis** 架构演进有关。

**早期的设计（RedisConnection 大杂烩）：**
在 Spring Data Redis 1.x 时代，Redis 的命令还不是很多。Spring 官方把所有 Redis 的原生命令（比如 `GET`、`SET`、`EXISTS`、
`HGET`、`LPUSH`... 几百个命令）**全部**塞进了 `RedisConnection` 这个庞大的接口里。
这导致 `RedisConnection` 变成了一个包含几百个方法的“巨无霸”接口。代码变得极其臃肿，而且一旦 Redis 出了新功能（比如后来的
Stream、Geo 空间索引），就要去改这个核心接口。

**现在的设计（单一职责原则拆分）：**
到了 Spring Data Redis 2.x（你项目用的版本），官方意识到这样不行，违背了面向对象设计里的“单一职责原则”。
于是，他们把命令分门别类，拆分成了不同的“小指挥官”：

* **Key 相关的命令**（比如 `exists` 检查键是否存在, `expire` 设置过期时间, `del` 删除键）：被归类到了 `keyCommands()` 里。
* **String 相关的命令**（比如 `get`, `set`）：被归类到了 `stringCommands()` 里。
* **Hash 相关的命令**（比如 `hGet`, `hSet`）：被归类到了 `hashCommands()` 里。

**所以，官方把直接在 `connection` 上调用的老方法打上了 `@Deprecated`（过时）标签。**

**你现在的代码该怎么改？**

之前提示过时的老写法：

```java
// ❌ 提示过时，不推荐
connection.exists(raw(keySerializer, CacheNames.IPEXPIRE_KEY + WebUtil.getIP()));
```

现在推荐的新写法：

```java
// ✅ 推荐写法：先呼叫管 Key 的小指挥官，再让他去执行 exists
connection.keyCommands().exists(raw(keySerializer, CacheNames.IPEXPIRE_KEY + WebUtil.getIP()));
```

如果是查在线人数的 `get`，就应该改成：

```java
connection.stringCommands().get(...);
```

**总结：**

* `RedisCallback` 是为了让你能安全、方便地**独占一个连接连续发命令**，而不用操心连接的借还。
* 提示过时换用 `keyCommands()`，是 Spring 官方为了**整理代码结构**，把原本揉在一起的几百个命令，按功能分类放进了不同的子接口里，让代码看起来更清晰、更好维护。

## 1.3 这个命令如何理解？

上面的代码，是使用lambdaExpress进行优化后的，所以先看一下原生的代码：

```java
public class AuthUtils {

    private final RedisTemplate<String, Object> redisTemplate;

    public List<Object> pipelined() {
        redisTemplate.executePipelined(new RedisCallback<Object>() {

            @Override
            public @Nullable Object doInRedis(RedisConnection connection) throws DataAccessException {
                connection.keyCommands().exists();
                return null;
            }
        }, redisTemplate.getValueSerializer());

        return null;
    }

}
```

