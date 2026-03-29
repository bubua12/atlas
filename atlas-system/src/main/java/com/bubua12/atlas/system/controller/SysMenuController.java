package com.bubua12.atlas.system.controller;

import com.bubua12.atlas.common.core.result.CommonResult;
import com.bubua12.atlas.system.repository.SysMenu;
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

    /**
     * 查询菜单树列表。
     *
     * @return 菜单树数据
     */
    @GetMapping
    public CommonResult<List<SysMenu>> list() {
        return CommonResult.success(sysMenuService.listTree());
    }

    /**
     * 根据菜单ID查询详情。
     *
     * @param menuId 菜单ID
     * @return 菜单详情
     */
    @GetMapping("/{menuId}")
    public CommonResult<SysMenu> getById(@PathVariable Long menuId) {
        return CommonResult.success(sysMenuService.getById(menuId));
    }

    /**
     * 新增菜单。
     *
     * @param menu 菜单信息
     * @return 执行结果
     */
    @PostMapping
    public CommonResult<Void> create(@RequestBody SysMenu menu) {
        sysMenuService.create(menu);
        return CommonResult.success();
    }

    /**
     * 更新菜单。
     *
     * @param menu 菜单信息
     * @return 执行结果
     */
    @PutMapping
    public CommonResult<Void> update(@RequestBody SysMenu menu) {
        sysMenuService.update(menu);
        return CommonResult.success();
    }

    /**
     * 删除菜单。
     *
     * @param menuId 菜单ID
     * @return 执行结果
     */
    @DeleteMapping("/{menuId}")
    public CommonResult<Void> delete(@PathVariable Long menuId) {
        sysMenuService.delete(menuId);
        return CommonResult.success();
    }
}
