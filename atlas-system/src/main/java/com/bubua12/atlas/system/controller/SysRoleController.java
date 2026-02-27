package com.bubua12.atlas.system.controller;

import com.bubua12.atlas.common.core.result.CommonResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/role")
public class SysRoleController {

    @GetMapping
    public CommonResult<?> list() {
        return CommonResult.ok();
    }

    @GetMapping("/{roleId}")
    public CommonResult<?> getById(@PathVariable Long roleId) {
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

    @DeleteMapping("/{roleId}")
    public CommonResult<Void> delete(@PathVariable Long roleId) {
        return CommonResult.ok();
    }
}
