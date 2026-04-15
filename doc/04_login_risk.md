## 🔒 安全评估报告

基于代码审查，我发现了以下安全问题：

✅ SQL 注入：安全

- 使用 MyBatis-Plus 的 LambdaQueryWrapper，参数化查询
- 例如：wrapper.eq(SysUser::getUsername, username)
- 无 SQL 注入风险

❌ 密码安全：严重问题

问题1：密码比较方式错误
// PasswordLoginHandler.java:37
if (!request.getPassword().equals(user.getPassword())) {
throw new RuntimeException("密码错误");
}

- 注释说使用 BCrypt 加密，但代码是明文比较
- 如果数据库密码是 BCrypt 加密的，这个比较永远不会成功
- 如果数据库密码是明文的，这是严重安全漏洞

问题2：密码通过网络传输
// SysUserController.java:137
dto.setPassword(user.getPassword());

- UserDTO 包含密码字段，通过 Feign 在服务间传输
- 即使加密也不应该传输密码

❌ 用户枚举：中等风险

// PasswordLoginHandler.java:34
throw new RuntimeException("用户不存在: " + request.getUsername());
// 第38行
throw new RuntimeException("密码错误");

- 错误消息区分"用户不存在"和"密码错误"
- 攻击者可以枚举有效用户名

❌ 暴力破解：高风险

- 没有登录失败次数限制
- 没有账户锁定机制
- 没有强制验证码验证

###⚠️ 其他风险

- 缺少输入验证（用户名、密码格式）
- 缺少 CSRF 保护（需要检查前端）
- 缺少请求频率限制

  ---

需要立即修复的问题：

1. 使用 BCrypt 验证密码
2. 移除 UserDTO 中的密码字段
3. 统一错误消息
4. 添加登录失败限制