package com.bubua12.atlas.system.controller;

import com.bubua12.atlas.common.core.result.CommonResult;
import com.bubua12.atlas.system.entity.request.AssignMenusRequest;
import com.bubua12.atlas.system.entity.request.UpdateDataScopeRequest;
import com.bubua12.atlas.system.entity.request.AssignUsersRequest;
import com.bubua12.atlas.system.repository.SysRole;
import com.bubua12.atlas.system.repository.SysUser;
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

    /**
     * 查询角色列表。
     *
     * @return 角色列表
     */
    @GetMapping
    public CommonResult<List<SysRole>> list() {
        return CommonResult.success(sysRoleService.list());
    }

    /**
     * 根据角色ID查询详情。
     *
     * @param roleId 角色ID
     * @return 角色详情
     */
    @GetMapping("/{roleId}")
    public CommonResult<SysRole> getById(@PathVariable Long roleId) {
        return CommonResult.success(sysRoleService.getById(roleId));
    }

    /**
     * 新增角色。
     *
     * @param role 角色信息
     * @return 执行结果
     */
    @PostMapping
    public CommonResult<Void> create(@RequestBody SysRole role) {
        sysRoleService.create(role);
        return CommonResult.success();
    }

    /**
     * 更新角色。
     *
     * @param role 角色信息
     * @return 执行结果
     */
    @PutMapping
    public CommonResult<Void> update(@RequestBody SysRole role) {
        sysRoleService.update(role);
        return CommonResult.success();
    }

    /**
     * 删除角色。
     *
     * @param roleId 角色ID
     * @return 执行结果
     */
    @DeleteMapping("/{roleId}")
    public CommonResult<Void> delete(@PathVariable Long roleId) {
        sysRoleService.delete(roleId);
        return CommonResult.success();
    }

    /**
     * 获取角色下的用户列表
     *
     * @param roleId 角色ID
     * @return 角色下用户列表
     */
    @GetMapping("/{roleId}/users")
    public CommonResult<List<SysUser>> getRoleUsers(@PathVariable Long roleId) {
        return CommonResult.success(sysRoleService.getRoleUsers(roleId));
    }

    /**
     * 给角色分配用户，适用与批量绑定角色给一部分用户
     *
     * @param request 分配用户请求
     * @return 执行结果
     */
    @PutMapping("/users")
    public CommonResult<Void> assignUsers(@RequestBody AssignUsersRequest request) {
        sysRoleService.assignUsers(request.getRoleId(), request.getUserIds());
        return CommonResult.success();
    }

    /**
     * 获取角色已经绑定了的菜单权限
     *
     * @param roleId 角色ID
     * @return 菜单ID列表
     */
    @GetMapping("/{roleId}/menus")
    public CommonResult<List<Long>> getRoleMenuIds(@PathVariable Long roleId) {
        return CommonResult.success(sysRoleService.getRoleMenuIds(roleId));
    }

    /**
     * 给角色分配菜单
     *
     * @param request 分配菜单请求
     * @return 执行结果
     */
    @PutMapping("/menus")
    public CommonResult<Void> assignMenus(@RequestBody AssignMenusRequest request) {
        sysRoleService.assignMenus(request.getRoleId(), request.getMenuIds());
        return CommonResult.success();
    }

    /**
     * 更新角色数据权限
     *
     * @param request 更新数据权限请求
     * @return 执行结果
     */
    @PutMapping("/data-scope")
    public CommonResult<Void> updateDataScope(@RequestBody UpdateDataScopeRequest request) {
        sysRoleService.updateDataScope(request.getRoleId(), request.getDataScope(), request.getDeptIds());
        return CommonResult.success();
    }
}
