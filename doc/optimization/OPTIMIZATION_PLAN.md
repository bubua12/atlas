# Atlas 高优先级问题修复计划

## 修复顺序（按依赖关系）

### 阶段 1：基础配置优化（快速见效，无风险）✅
- [x] 1.4 配置 Feign 超时和重试
- [x] 1.6 实现缓存雪崩防护（随机 TTL）
- [x] 1.7 修复 Token 刷新竞态条件
- [x] 配置 Redis 连接池

### 阶段 2：ThreadLocal 优化（影响范围大，需要测试）✅
- [x] 1.10 优化 ThreadLocal 使用（轻量化 + InheritableThreadLocal）
- [x] 更新 SecurityContextHolder（使用 InheritableThreadLocal）
- [x] 更新 UserContextInterceptor（移除 Redis 查询）
- [x] 更新 WebMvcConfig（移除 RedisService 依赖）
- [x] 更新 PreAuthorizeAspect（适配新的上下文）
- [x] 配置 AsyncConfig（TaskDecorator 传递上下文）

### 阶段 3：性能优化（需要配合阶段 2）
- [x] 1.3 减少 Token 验证双重查询

### 阶段 4：安全加固（需要配合阶段 3）
- [x] 1.1 实现网关请求签名验证
- [x] 1.2 为 Feign 接口添加权限保护

### 阶段 5：权限系统优化
- [x] 1.5 优化权限加载（通配符 + 懒加载）
- [x] 1.8 实现权限缓存失效通知

### 阶段 6：数据权限修复
- [x] 1.11 修复 SQL 改写解析失败

---

## 当前进度

✅ 阶段 1 完成
✅ 阶段 2 完成
✅ 阶段 3 完成
✅ 阶段 4 完成
✅ 阶段 5 完成
✅ 阶段 6 完成
