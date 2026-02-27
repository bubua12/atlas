package com.bubua12.atlas.system.controller;

import com.bubua12.atlas.common.core.result.CommonResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/menu")
public class SysMenuController {

    @GetMapping
    public CommonResult<?> list() {
        return CommonResult.ok();
    }

    @GetMapping("/{menuId}")
    public CommonResult<?> getById(@PathVariable Long menuId) {
        return CommonResult.ok();
    }

    @PostMapping
    public CommonResult<Void> create() {
        return CommonResult.ok();
    }

    @PutMapping
    public CommonResult<Void> update() {
        return CommonResult.ok();
    }

    @DeleteMapping("/{menuId}")
    public CommonResult<Void> delete(@PathVariable Long menuId) {
        return CommonResult.ok();
    }
}
