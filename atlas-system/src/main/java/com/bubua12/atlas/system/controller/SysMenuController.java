package com.bubua12.atlas.system.controller;

import com.bubua12.atlas.common.core.result.CommonResult;
import com.bubua12.atlas.system.entity.SysMenu;
import com.bubua12.atlas.system.service.SysMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单管理控制器
 */
@RestController
@RequestMapping("/menu")
@RequiredArgsConstructor
public class SysMenuController {

    private final SysMenuService sysMenuService;

    @GetMapping
    public CommonResult<List<SysMenu>> list() {
        return CommonResult.success(sysMenuService.listTree());
    }

    @GetMapping("/{menuId}")
    public CommonResult<SysMenu> getById(@PathVariable Long menuId) {
        return CommonResult.success(sysMenuService.getById(menuId));
    }

    @PostMapping
    public CommonResult<Void> create(@RequestBody SysMenu menu) {
        sysMenuService.create(menu);
        return CommonResult.success();
    }

    @PutMapping
    public CommonResult<Void> update(@RequestBody SysMenu menu) {
        sysMenuService.update(menu);
        return CommonResult.success();
    }

    @DeleteMapping("/{menuId}")
    public CommonResult<Void> delete(@PathVariable Long menuId) {
        sysMenuService.delete(menuId);
        return CommonResult.success();
    }
}
