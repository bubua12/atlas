# 接口限流增强方案（可选 IP 维度）

## 1. 目标

在现有 `@RedisLimit` 的“接口维度限流”基础上，增加“可选 IP 维度限流”能力，满足以下行为：

- 默认保持现有行为不变（仅接口维度限流）
- 当配置开启 IP 维度限流后，对同一接口额外按 IP 做一次限流校验
- 命中任一限流规则时，直接返回限流提示，不放行业务方法
- 兼容现有操作日志顺序控制（限流切面在外层时，限流命中不写操作日志）

## 2. 当前实现现状

- 注解：`@RedisLimit(key, permitsPerSecond, expire, msg)`，目前只有接口维度参数
- 切面：`RedisLimitAspect` 中按 `类名:方法名:key` 生成 Redis Key，执行 Lua 脚本计数
- 触发限流：直接返回 `CommonResult.fail(msg)`，不抛业务异常

## 3. 设计原则

- 向后兼容：老注解不改也能正常工作
- 显式开关：IP 限流必须由配置启用，避免无感知变更
- 低侵入：尽量复用现有 Lua 与切面流程，不新增复杂中间层
- 可扩展：预留后续“用户维度/租户维度”等多维限流能力

## 4. 方案设计

### 4.1 配置模型

新增限流配置（建议放在 `atlas.redis.limit` 下）：

- `enableIpDimension`：是否启用 IP 维度限流（默认 `false`）
- `ipPermitsPerSecond`：IP 维度每窗口允许次数（默认可与接口维度一致）
- `ipExpire`：IP 维度窗口秒数（默认可与接口维度一致）
- `ipKeyPrefix`：IP 维度 key 前缀（默认 `ip-limit`）

说明：

- 当 `enableIpDimension=false`：只执行接口维度限流（当前逻辑）
- 当 `enableIpDimension=true`：先做接口维度，再做接口+IP 维度（双重闸门）

### 4.2 Key 设计

现有接口维度 Key（保留）：

- `limit:{class}:{method}:{annotationKey}`

新增 IP 维度 Key：

- `limit-ip:{class}:{method}:{annotationKey}:{clientIp}`

其中 `clientIp` 使用现有工具获取真实 IP（处理代理头），并做安全标准化：

- 去空格、去端口、IPv6 统一格式
- 空值兜底为 `unknown`

### 4.3 切面执行流程

对标注 `@RedisLimit` 的方法，切面流程如下：

1. 解析注解参数（现有）
2. 执行接口维度限流（现有）
3. 若配置开启 IP 维度：执行接口+IP 维度限流（新增）
4. 任一维度返回超限：立即 `CommonResult.fail(msg)`（不 `proceed`）
5. 两个维度都通过：`proceed` 放行

### 4.4 参数优先级建议

为保证配置简洁与可控，建议优先级：

1. 注解参数（接口维度）：`permitsPerSecond`、`expire`
2. 配置参数（IP 维度）：`ipPermitsPerSecond`、`ipExpire`
3. 配置缺省时可回退注解参数（避免配置不全导致不可用）

## 5. 可选扩展（第二阶段）

若后续需要更细粒度控制，可在注解增加可选字段：

- `boolean enableIpLimit() default false`

并与全局配置组合：

- 全局开 + 注解开：启用 IP 限流
- 全局开 + 注解关：仅接口限流
- 全局关：全部不启用 IP 限流

这样可以做到“只给少数高风险接口开 IP 限流”。

## 6. 错误与日志策略

- 限流命中统一返回注解 `msg`
- 不额外抛异常，保持与现有返回风格一致
- 建议仅打印 `debug/info` 级限流命中摘要日志，避免刷日志
- 操作日志沿用当前 AOP 顺序策略：限流短路时不进入操作日志切面

## 7. 风险点与规避

- 代理链 IP 不准确：必须统一使用真实 IP 解析工具
- NAT 场景误伤：同一公网出口可能共享 IP，可通过 `ipPermitsPerSecond` 放宽
- Redis Key 膨胀：给 IP 维度 key 设置合理过期，控制窗口长度
- 配置误开影响：默认关闭 + 灰度环境先验证

## 8. 验收标准

开启前（默认）：

- 行为与当前完全一致

开启后：

- 同一接口总流量超限时返回限流提示
- 同一接口单 IP 超限时也返回限流提示
- 任一超限都不进入业务方法
- 限流命中场景不写操作日志入库

## 9. 落地步骤（仅方案，不改代码）

1. 增加限流配置类与默认值
2. 在 `RedisLimitAspect` 注入配置并补充 IP 维度分支
3. 复用现有 Lua 脚本执行第二次限流判断
4. 增加单元/集成测试（接口维度、IP 维度、双重限流）
5. 灰度验证并观察 Redis key 增长与限流命中率

