package com.bubua12.atlas.system.controller;

import com.bubua12.atlas.common.core.result.CommonResult;
import com.bubua12.atlas.system.entity.SysDept;
import com.bubua12.atlas.system.service.SysDeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门管理控制器
 */
@RestController
@RequestMapping("/dept")
@RequiredArgsConstructor
public class SysDeptController {

    private final SysDeptService sysDeptService;

    @GetMapping
    public CommonResult<List<SysDept>> list() {
        return CommonResult.success(sysDeptService.listTree());
    }

    @GetMapping("/{deptId}")
    public CommonResult<SysDept> getById(@PathVariable Long deptId) {
        return CommonResult.success(sysDeptService.getById(deptId));
    }

    @PostMapping
    public CommonResult<Void> create(@RequestBody SysDept dept) {
        sysDeptService.create(dept);
        return CommonResult.success();
    }

    @PutMapping
    public CommonResult<Void> update(@RequestBody SysDept dept) {
        sysDeptService.update(dept);
        return CommonResult.success();
    }

    @DeleteMapping("/{deptId}")
    public CommonResult<Void> delete(@PathVariable Long deptId) {
        sysDeptService.delete(deptId);
        return CommonResult.success();
    }
}
