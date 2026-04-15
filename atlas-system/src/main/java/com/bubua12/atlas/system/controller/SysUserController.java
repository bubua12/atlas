package com.bubua12.atlas.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bubua12.atlas.api.system.dto.SysUserDTO;
import com.bubua12.atlas.api.system.dto.UserDTO;
import com.bubua12.atlas.common.core.domain.PageQuery;
import com.bubua12.atlas.common.core.result.CommonResult;
import com.bubua12.atlas.common.security.annotation.InternalApi;
import com.bubua12.atlas.common.security.annotation.RequiresPermission;
import com.bubua12.atlas.system.converter.SysUserConverter;
import com.bubua12.atlas.system.entity.request.AssignRolesRequest;
import com.bubua12.atlas.system.repository.SysUser;
import com.bubua12.atlas.system.service.SysMenuService;
import com.bubua12.atlas.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService sysUserService;

    private final SysMenuService sysMenuService;

    private final SysUserConverter sysUserConverter;

    /**
     * 用户列表分页
     *
     * @param query PageQuery
     * @return CommonResult<IPage<SysUser>>
     */
    @RequiresPermission("system:user:list")
    @GetMapping
    public CommonResult<IPage<SysUser>> list(PageQuery query) {
        return CommonResult.success(sysUserService.list(query));
    }

    /**
     * 查询用户
     *
     * @param userId userId
     * @return CommonResult
     */
    @RequiresPermission("system:user:query")
    @GetMapping("/{userId}")
    public CommonResult<SysUser> getById(@PathVariable Long userId) {
        return CommonResult.success(sysUserService.getById(userId));
    }

    /**
     * 新增用户
     *
     * @param user user
     * @return CommonResult
     */
    @RequiresPermission("system:user:add")
    @PostMapping
    public CommonResult<Void> create(@RequestBody SysUser user) {
        sysUserService.create(user);
        return CommonResult.success();
    }

    /**
     * 更新用户
     *
     * @param user user
     * @return CommonResult
     */
    @RequiresPermission("system:user:edit")
    @PutMapping
    public CommonResult<Void> update(@RequestBody SysUser user) {
        sysUserService.update(user);
        return CommonResult.success();
    }

    /**
     * 删除用户
     *
     * @param userId userId
     * @return CommonResult
     */
    @RequiresPermission("system:user:remove")
    @DeleteMapping("/{userId}")
    public CommonResult<Void> delete(@PathVariable Long userId) {
        sysUserService.delete(userId);
        return CommonResult.success();
    }


    /**
     * Feign接口
     *
     * @param phone 手机号码
     * @return CommonResult<UserDTO>
     */
    @GetMapping("/phone/{phone}")
    @InternalApi(allowedServices = {"atlas-auth"})
    public CommonResult<UserDTO> getUserByPhone(@PathVariable String phone) {
        SysUser user = sysUserService.getByPhone(phone);
        if (user == null) {
            return CommonResult.fail("User not found");
        }
        UserDTO dto = convertToUserDTO(user);
        return CommonResult.success(dto);
    }

    /**
     * 适用于OAuth2场景下首次创建用户
     *
     * @param sysUserDTO 用户信息
     * @return 执行结果
     */
    @PostMapping("/oauth2/createUser")
    @InternalApi(allowedServices = {"atlas-auth"})
    public CommonResult<Void> createUserOAuth2(@RequestBody SysUserDTO sysUserDTO) {

        SysUser sysUser = sysUserConverter.vo2po(sysUserDTO);
        sysUserService.create(sysUser);

        return CommonResult.success();
    }

    /**
     * 根据用户名查询用户信息。
     *
     * @param username 用户名
     * @return 用户信息
     */
    @GetMapping("/info/{username}")
    @InternalApi(allowedServices = {"atlas-auth"})
    public CommonResult<UserDTO> getUserByUsername(@PathVariable String username) {
        SysUser user = sysUserService.getByUsername(username);
        if (user == null) {
            return CommonResult.fail("User not found");
        }
        UserDTO dto = convertToUserDTO(user);
        return CommonResult.success(dto);
    }

    /**
     * 根据 OpenId 查询用户信息。
     *
     * @param openId 第三方平台唯一标识
     * @return 用户信息
     */
    @GetMapping("/openid/{openId}")
    @InternalApi(allowedServices = {"atlas-auth"})
    public CommonResult<UserDTO> getUserByOpenId(@PathVariable String openId) {
        SysUser user = sysUserService.getByOpenId(openId);
        if (user == null) {
            return CommonResult.fail("User not found");
        }
        UserDTO dto = convertToUserDTO(user);
        return CommonResult.success(dto);
    }

    /**
     * 校验用户密码是否正确。
     *
     * @param username 用户名
     * @param password 明文密码
     * @return true 表示密码正确
     */
    @PostMapping("/verify-password")
    @InternalApi(allowedServices = {"atlas-auth"})
    public CommonResult<Boolean> verifyPassword(@RequestParam("username") String username, @RequestParam("password") String password) {
        boolean valid = sysUserService.verifyPassword(username, password);
        return CommonResult.success(valid);
    }

    /**
     * 给用户分配角色
     */
    @RequiresPermission("system:user:edit")
    @PutMapping("/roles")
    public CommonResult<Void> assignRoles(@RequestBody AssignRolesRequest request) {
        sysUserService.assignRoles(request.getUserId(), request.getRoleIds());
        return CommonResult.success();
    }

    /**
     * 获取用户的角色ID列表
     */
    @RequiresPermission("system:user:query")
    @GetMapping("/{userId}/roles")
    public CommonResult<List<Long>> getUserRoleIds(@PathVariable Long userId) {
        return CommonResult.success(sysUserService.getUserRoleIds(userId));
    }

    /**
     * 将系统用户转换为远程调用用户DTO。
     *
     * @param user 系统用户
     * @return 用户DTO
     */
    private UserDTO convertToUserDTO(SysUser user) {
        UserDTO dto = new UserDTO();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setStatus(user.getStatus());
        dto.setDeptId(user.getDeptId());

        // 查询并设置数据权限
        Integer dataScope = sysUserService.getUserDataScope(user.getUserId());
        dto.setDataScope(dataScope != null ? dataScope : 4);

        // 查询并设置权限
        List<String> perms = sysMenuService.getPermsByUserId(user.getUserId());
        if (perms != null && !perms.isEmpty()) {
            dto.setPermissions(new HashSet<>(perms));
        }
        return dto;
    }
}
