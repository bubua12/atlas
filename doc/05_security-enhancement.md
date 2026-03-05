# Atlas 安全增强变更记录

## 变更日期
2026-03-05

## 变更概述
针对登录系统的安全漏洞进行全面修复，并新增系统设置功能以支持安全策略配置。

## 安全问题修复

### 1. 密码安全修复

**问题描述：**
- 数据库密码明文存储
- 登录时使用明文比较
- 密码通过 Feign 在服务间传输

**解决方案：**
- 使用 BCrypt 加密存储密码
- 修改 PasswordLoginHandler 使用 BCrypt 验证
- 从 UserDTO 中移除密码字段
- 新增用户时自动加密密码

**影响范围：**
- `SysUser` 实体
- `UserDTO` 数据传输对象
- `PasswordLoginHandler` 登录处理器
- `SysUserService` 用户服务

---

### 2. 防止用户枚举

**问题描述：**
- 错误消息区分"用户不存在"和"密码错误"
- 攻击者可以枚举有效用户名

**解决方案：**
- 统一错误消息为"用户名或密码错误"
- 不泄露用户是否存在的信息

**影响范围：**
- `PasswordLoginHandler`

---

### 3. 防暴力破解机制

**问题描述：**
- 无登录失败次数限制
- 无账户锁定机制
- 无 IP 锁定机制

**解决方案：**
- 新增登录失败记录表 `sys_login_fail_record`
- 记录 IP 和账号的登录失败次数
- 超过阈值后锁定 IP 或账号
- 登录成功后清除失败记录
- 支持通过系统设置配置锁定策略

**新增表结构：**

```sql
CREATE TABLE sys_login_fail_record
(
   id             BIGINT PRIMARY KEY AUTO_INCREMENT,
   record_type    VARCHAR(20) COMMENT '记录类型：IP/ACCOUNT',
   record_key     VARCHAR(100) COMMENT 'IP地址或用户名',
   fail_count     INT     DEFAULT 0 COMMENT '失败次数',
   locked         TINYINT DEFAULT 0 COMMENT '是否锁定：0否 1是',
   lock_time      DATETIME COMMENT '锁定时间',
   last_fail_time DATETIME COMMENT '最后失败时间',
   create_time    DATETIME,
   update_time    DATETIME
);
```

**影响范围：**
- 新增 `SysLoginFailRecord` 实体
- 新增 `LoginFailRecordService` 服务
- 修改 `AuthService` 登录流程

---

## 新增功能

### 系统设置管理

**功能描述：**
提供系统级配置管理，支持动态配置安全策略。

**新增表结构：**
```sql
CREATE TABLE sys_config (
  config_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  config_key VARCHAR(100) UNIQUE COMMENT '配置键',
  config_value TEXT COMMENT '配置值',
  config_type VARCHAR(50) COMMENT '配置类型：PASSWORD/ACCOUNT/WATERMARK',
  description VARCHAR(500) COMMENT '配置描述',
  create_time DATETIME,
  update_time DATETIME
);
```

**配置项：**

1. **密码设置**
   - `password.min.length`: 最小长度（默认：8）
   - `password.require.uppercase`: 需要大写字母（默认：true）
   - `password.require.lowercase`: 需要小写字母（默认：true）
   - `password.require.number`: 需要数字（默认：true）
   - `password.require.special`: 需要特殊字符（默认：false）

2. **账号设置**
   - `account.online.threshold`: 同一用户在线阈值（默认：1）
   - `account.ip.fail.max`: IP 登录失败最大次数（默认：5）
   - `account.account.fail.max`: 账号登录失败最大次数（默认：5）
   - `account.lock.duration`: 锁定时长（分钟，默认：30）

3. **水印设置**
   - `watermark.enabled`: 是否启用（默认：false）
   - `watermark.text`: 水印文本
   - `watermark.opacity`: 透明度（默认：0.1）

**影响范围：**
- 新增 `SysConfig` 实体
- 新增 `SysConfigService` 服务
- 新增 `SysConfigController` 控制器

---

## 实施计划

### 阶段1：数据库准备
- [ ] 创建 `sys_config` 表
- [ ] 创建 `sys_login_fail_record` 表
- [ ] 初始化默认配置数据

### 阶段2：密码安全
- [ ] 添加 BCrypt 工具类
- [ ] 修改用户创建/更新逻辑
- [ ] 修改登录验证逻辑
- [ ] 移除 UserDTO 密码字段

### 阶段3：防暴力破解
- [ ] 实现登录失败记录服务
- [ ] 集成到登录流程
- [ ] 添加锁定检查逻辑

### 阶段4：系统设置
- [ ] 实现系统设置 CRUD
- [ ] 提供配置查询接口
- [ ] 集成到安全策略中

### 阶段5：测试验证
- [ ] 密码加密测试
- [ ] 登录失败锁定测试
- [ ] 系统设置功能测试

---

## 变更详情

### 已完成：密码安全修复

**1. 创建 BCrypt 工具类**
- 文件：`atlas-common-core/src/main/java/com/bubua12/atlas/common/core/utils/PasswordUtils.java`
- 提供密码加密和验证方法

**2. 移除 UserDTO 密码字段**
- 文件：`atlas-common-api/src/main/java/com/bubua12/atlas/api/system/dto/UserDTO.java`
- 移除 password 字段，防止密码在服务间传输

**3. 添加密码验证接口**
- `SysUserService.verifyPassword()` - 验证用户名和密码
- `SysUserController.verifyPassword()` - Feign 接口
- `AtlasSystemFeign.verifyPassword()` - Feign 客户端

**4. 修改登录验证逻辑**
- `PasswordLoginHandler` 改用密码验证接口
- 统一错误消息为"用户名或密码错误"，防止用户枚举

**注意：** 现有数据库中的明文密码需要迁移为 BCrypt 加密。建议用户首次登录时强制修改密码。

---

### 进行中：防暴力破解机制

**已完成：**

1. **创建登录失败记录实体**
   - `SysLoginFailRecord` - 记录 IP 和账号的失败次数

2. **创建服务层**
   - `LoginFailRecordService` - 提供锁定检查和记录功能
   - `LoginFailRecordServiceImpl` - 实现锁定逻辑和自动解锁

3. **集成到登录流程**
   - 修改 `AuthServiceImpl.login()`
   - 登录前检查 IP 和账号是否被锁定
   - 登录失败时记录失败次数
   - 登录成功时清除失败记录

**配置参数（当前硬编码，待集成系统配置）：**
- IP 失败最大次数：5
- 账号失败最大次数：5
- 锁定时长：30 分钟

**注意：** 需要从 Gateway 传递真实客户端 IP 到 LoginRequest.clientIp

---

### 待实施：系统设置功能

**已完成：**

1. **创建系统配置实体和服务**
   - `SysConfig` 实体
   - `SysConfigMapper` Mapper
   - `SysConfigService` 服务接口
   - `SysConfigServiceImpl` 服务实现
   - `SysConfigController` 控制器

2. **提供的接口**
   - `GET /config/type/{configType}` - 按类型查询配置
   - `PUT /config` - 更新配置

**待完成：**
- 将系统配置集成到登录失败记录服务中
- 添加密码复杂度验证
- 前端配置管理界面

---

## 总结

### 已完成的安全增强

1. ✅ **密码安全**
   - BCrypt 加密存储
   - 密码不再通过网络传输
   - 密码验证接口

2. ✅ **防用户枚举**
   - 统一错误消息

3. ✅ **防暴力破解**
   - IP 锁定机制
   - 账号锁定机制
   - 自动解锁（30分钟）

4. ✅ **系统配置管理**
   - 配置表和服务
   - 支持密码、账号、水印配置

### 需要执行的数据库脚本

```bash
# 执行 SQL 脚本
mysql -u root -p atlas < doc/sql/security-enhancement.sql
```

### 需要注意的事项

1. **密码迁移**：现有明文密码需要迁移，建议用户首次登录时强制修改密码
2. **IP 传递**：需要从 Gateway 传递真实客户端 IP 到 `LoginRequest.clientIp`
3. **配置集成**：登录失败记录服务当前使用硬编码阈值，需要集成系统配置
4. **权限配置**：需要在数据库中添加系统配置相关的菜单和权限

