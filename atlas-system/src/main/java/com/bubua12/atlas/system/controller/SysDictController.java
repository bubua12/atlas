package com.bubua12.atlas.system.controller;

import com.bubua12.atlas.common.core.result.CommonResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dict")
public class SysDictController {

    // ===== Dict Type =====

    @GetMapping("/type")
    public CommonResult<?> listTypes() {
        return CommonResult.ok();
    }

    @GetMapping("/type/{dictId}")
    public CommonResult<?> getTypeById(@PathVariable Long dictId) {
        return CommonResult.ok();
    }

    @PostMapping("/type")
    public CommonResult<Void> createType() {
        return CommonResult.ok();
    }

    @PutMapping("/type")
    public CommonResult<Void> updateType() {
        return CommonResult.ok();
    }

    @DeleteMapping("/type/{dictId}")
    public CommonResult<Void> deleteType(@PathVariable Long dictId) {
        return CommonResult.ok();
    }

    // ===== Dict Data =====

    @GetMapping("/data")
    public CommonResult<?> listData() {
        return CommonResult.ok();
    }

    @GetMapping("/data/{dictCode}")
    public CommonResult<?> getDataById(@PathVariable Long dictCode) {
        return CommonResult.ok();
    }

    @PostMapping("/data")
    public CommonResult<Void> createData() {
        return CommonResult.ok();
    }

    @PutMapping("/data")
    public CommonResult<Void> updateData() {
        return CommonResult.ok();
    }

    @DeleteMapping("/data/{dictCode}")
    public CommonResult<Void> deleteData(@PathVariable Long dictCode) {
        return CommonResult.ok();
    }
}
