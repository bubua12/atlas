package com.bubua12.atlas.system.controller;

import com.bubua12.atlas.common.core.result.CommonResult;
import com.bubua12.atlas.system.entity.SysRole;
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
}
