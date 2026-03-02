package com.bubua12.atlas.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bubua12.atlas.api.system.dto.UserDTO;
import com.bubua12.atlas.common.core.domain.PageQuery;
import com.bubua12.atlas.common.core.result.CommonResult;
import com.bubua12.atlas.common.security.annotation.RequiresPermission;
import com.bubua12.atlas.system.entity.SysUser;
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

    // fixme 这样写为什么也可以进行自动注入
    private final SysUserService sysUserService;

    private final SysMenuService sysMenuService;

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
    public CommonResult<Void> delete(@PathVariable("userId") Long userId) {
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
    public CommonResult<UserDTO> getUserByPhone(@PathVariable("phone") String phone) {
        SysUser user = sysUserService.getByPhone(phone);
        if (user == null) {
            return CommonResult.fail("User not found");
        }
        UserDTO dto = convertToUserDTO(user);
        return CommonResult.success(dto);
    }


    @GetMapping("/info/{username}")
    public CommonResult<UserDTO> getUserByUsername(@PathVariable("username") String username) {
        SysUser user = sysUserService.getByUsername(username);
        if (user == null) {
            return CommonResult.fail("User not found");
        }
        UserDTO dto = convertToUserDTO(user);
        return CommonResult.success(dto);
    }


    @GetMapping("/openid/{openId}")
    public CommonResult<UserDTO> getUserByOpenId(@PathVariable("openId") String openId) {
        SysUser user = sysUserService.getByOpenId(openId);
        if (user == null) {
            return CommonResult.fail("User not found");
        }
        UserDTO dto = convertToUserDTO(user);
        return CommonResult.success(dto);
    }

    private UserDTO convertToUserDTO(SysUser user) {
        UserDTO dto = new UserDTO();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setPassword(user.getPassword());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setStatus(user.getStatus());
        dto.setDeptId(user.getDeptId());

        // 查询并设置权限
        List<String> perms = sysMenuService.getPermsByUserId(user.getUserId());
        if (perms != null && !perms.isEmpty()) {
            dto.setPermissions(new HashSet<>(perms));
        }
        return dto;
    }
}
