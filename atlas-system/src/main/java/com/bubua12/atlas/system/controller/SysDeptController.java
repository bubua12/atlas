package com.bubua12.atlas.system.controller;

import com.bubua12.atlas.common.core.result.CommonResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dept")
public class SysDeptController {

    @GetMapping
    public CommonResult<?> list() {
        return CommonResult.ok();
    }

    @GetMapping("/{deptId}")
    public CommonResult<?> getById(@PathVariable Long deptId) {
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

    @DeleteMapping("/{deptId}")
    public CommonResult<Void> delete(@PathVariable Long deptId) {
        return CommonResult.ok();
    }
}
