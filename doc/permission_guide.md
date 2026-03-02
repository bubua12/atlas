# @RequiresPermission 权限控制使用指南

`@RequiresPermission` 是本项目（Atlas）中用于控制接口访问权限的核心注解。它基于 AOP（面向切面编程）实现，能够拦截标有该注解的方法，并在执行前验证当前登录用户是否拥有指定的权限标识。

## 1. 核心原理

当一个请求到达 Controller 时，权限控制的流程如下：

1.  **拦截请求**：`PreAuthorizeAspect` 切面拦截所有标有 `@RequiresPermission` 的方法。
2.  **获取用户**：从 `SecurityContextHolder`（ThreadLocal）中获取当前登录用户的 Token。
3.  **查询权限**：根据 Token 从 Redis 中获取用户的完整权限列表（`Set<String>`）。
4.  **匹配校验**：
    *   **超级管理员**（User ID = 1）：直接放行，拥有所有权限。
    *   **普通用户**：检查用户的权限列表中是否包含注解指定的权限标识。
    *   **校验失败**：抛出 `BusinessException` (403 Forbidden)。
    *   **校验成功**：放行请求，执行 Controller 逻辑。

## 2. 如何使用

### 2.1 在 Controller 方法上添加注解

在需要权限控制的接口方法上，添加 `@RequiresPermission` 注解，并指定唯一的权限标识字符串。

**示例代码：**

```java
import com.bubua12.atlas.common.security.annotation.RequiresPermission;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/system/user")
public class SysUserController {

    /**
     * 查询用户列表
     * 需要权限: system:user:list
     */
    @RequiresPermission("system:user:list")
    @GetMapping("/list")
    public CommonResult<List<SysUser>> list() {
        return CommonResult.success(sysUserService.list());
    }

    /**
     * 新增用户
     * 需要权限: system:user:add
     */
    @RequiresPermission("system:user:add")
    @PostMapping
    public CommonResult<Void> add(@RequestBody SysUser user) {
        sysUserService.add(user);
        return CommonResult.success();
    }
    
    /**
     * 删除用户
     * 需要权限: system:user:remove
     */
    @RequiresPermission("system:user:remove")
    @DeleteMapping("/{userId}")
    public CommonResult<Void> remove(@PathVariable Long userId) {
        sysUserService.delete(userId);
        return CommonResult.success();
    }
}
```

### 2.2 权限标识命名规范

建议采用 `模块:资源:操作` 的格式，例如：

*   `system:user:list` - 系统模块-用户资源-查询列表
*   `system:role:add` - 系统模块-角色资源-新增
*   `monitor:server:view` - 监控模块-服务器资源-查看

### 2.3 数据库配置

为了让权限生效，你需要在数据库的 `sys_menu` 表中配置对应的菜单或按钮，并设置 `perms` 字段。

**SQL 示例：**

```sql
-- 添加“用户管理”菜单，权限标识为 system:user:list
INSERT INTO sys_menu (menu_name, parent_id, menu_type, perms, ...)
VALUES ('用户管理', 1, 'C', 'system:user:list', ...);

-- 添加“用户新增”按钮，权限标识为 system:user:add
INSERT INTO sys_menu (menu_name, parent_id, menu_type, perms, ...)
VALUES ('用户新增', 10, 'F', 'system:user:add', ...);
```

然后，在 `sys_role_menu` 表中将这些菜单 ID 关联到对应的角色 ID。

## 3. 效果演示

### 场景一：无权访问

*   **用户**：普通员工（只拥有 `system:user:list` 权限）
*   **操作**：尝试调用 `/system/user` 的 `DELETE` 接口（需要 `system:user:remove` 权限）。
*   **结果**：
    *   切面检测到用户权限列表中没有 `system:user:remove`。
    *   抛出异常：`BusinessException: 权限不足`。
    *   前端收到 HTTP 状态码 **403** 或 JSON 响应 `{ "code": 403, "msg": "权限不足" }`。

### 场景二：有权访问

*   **用户**：普通员工（拥有 `system:user:list` 权限）
*   **操作**：调用 `/system/user/list` 接口。
*   **结果**：
    *   切面检测通过。
    *   Controller 方法正常执行，返回用户列表数据。

### 场景三：超级管理员

*   **用户**：Admin（User ID = 1）
*   **操作**：调用任何接口。
*   **结果**：
    *   切面检测到 ID 为 1，直接放行。
    *   即使数据库中没有配置该权限，管理员也能访问。

## 4. 常见问题排查

如果发现注解不生效（明明有权限却报 403，或明明没权限却能访问）：

1.  **检查 Redis 数据**：登录后查看 Redis 中 `auth:token:xxx` 对应的 `LoginUser` 对象，确认 `permissions` 集合中是否包含你需要的权限标识。
2.  **检查注解拼写**：Controller 上的字符串是否与数据库 `sys_menu` 表中的 `perms` 字段完全一致（区分大小写，注意空格）。
3.  **检查 AOP 代理**：确保 Controller 类被 Spring 管理（加了 `@RestController`），且方法是 `public` 的。内部自调用（this.method()）不会触发 AOP。
4.  **检查网关传递**：确保网关正确传递了 `Authorization` 头或 `X-User-Id`，且拦截器 `UserContextInterceptor` 正常工作（Debug 看 ThreadLocal 是否有值）。
