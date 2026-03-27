package com.bubua12.atlas.system.controller;

import com.bubua12.atlas.common.core.result.CommonResult;
import com.bubua12.atlas.system.dto.AssignMenusRequest;
import com.bubua12.atlas.system.dto.AssignUsersRequest;
import com.bubua12.atlas.system.dto.UpdateDataScopeRequest;
import com.bubua12.atlas.system.entity.SysRole;
import com.bubua12.atlas.system.entity.SysUser;
import com.bubua12.atlas.system.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理控制器
 */
@RestController
@RequestMapping("/role")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleService sysRoleService;

    @GetMapping
    public CommonResult<List<SysRole>> list() {
        return CommonResult.success(sysRoleService.list());
    }

    @GetMapping("/{roleId}")
    public CommonResult<SysRole> getById(@PathVariable Long roleId) {
        return CommonResult.success(sysRoleService.getById(roleId));
    }

    @PostMapping
    public CommonResult<Void> create(@RequestBody SysRole role) {
        sysRoleService.create(role);
        return CommonResult.success();
    }

    @PutMapping
    public CommonResult<Void> update(@RequestBody SysRole role) {
        sysRoleService.update(role);
        return CommonResult.success();
    }

    @DeleteMapping("/{roleId}")
    public CommonResult<Void> delete(@PathVariable Long roleId) {
        sysRoleService.delete(roleId);
        return CommonResult.success();
    }

    /**
     * 获取角色下的用户列表
     */
    @GetMapping("/{roleId}/users")
    public CommonResult<List<SysUser>> getRoleUsers(@PathVariable Long roleId) {
        return CommonResult.success(sysRoleService.getRoleUsers(roleId));
    }

    /**
     * 给角色分配用户
     */
    @PutMapping("/users")
    public CommonResult<Void> assignUsers(@RequestBody AssignUsersRequest request) {
        sysRoleService.assignUsers(request.getRoleId(), request.getUserIds());
        return CommonResult.success();
    }

    /**
     * 获取角色的菜单ID列表
     */
    @GetMapping("/{roleId}/menus")
    public CommonResult<List<Long>> getRoleMenuIds(@PathVariable Long roleId) {
        return CommonResult.success(sysRoleService.getRoleMenuIds(roleId));
    }

    /**
     * 给角色分配菜单
     */
    @PutMapping("/menus")
    public CommonResult<Void> assignMenus(@RequestBody AssignMenusRequest request) {
        sysRoleService.assignMenus(request.getRoleId(), request.getMenuIds());
        return CommonResult.success();
    }

    /**
     * 更新角色数据权限
     */
    @PutMapping("/data-scope")
    public CommonResult<Void> updateDataScope(@RequestBody UpdateDataScopeRequest request) {
        sysRoleService.updateDataScope(request.getRoleId(), request.getDataScope(), request.getDeptIds());
        return CommonResult.success();
    }
}
