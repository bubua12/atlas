package com.bubua12.atlas.system.controller;

import com.bubua12.atlas.common.core.result.CommonResult;
import com.bubua12.atlas.system.repository.SysDept;
import com.bubua12.atlas.system.service.SysDeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门管理
 */
@RestController
@RequestMapping("/dept")
@RequiredArgsConstructor
public class SysDeptController {

    private final SysDeptService sysDeptService;

    /**
     * 查询部门树列表。
     *
     * @return 部门树数据
     */
    @GetMapping
    public CommonResult<List<SysDept>> list() {
        return CommonResult.success(sysDeptService.listTree());
    }

    /**
     * 根据部门ID查询详情。
     *
     * @param deptId 部门ID
     * @return 部门详情
     */
    @GetMapping("/{deptId}")
    public CommonResult<SysDept> getById(@PathVariable Long deptId) {
        return CommonResult.success(sysDeptService.getById(deptId));
    }

    /**
     * 新增部门。
     *
     * @param dept 部门信息
     * @return 执行结果
     */
    @PostMapping
    public CommonResult<Void> create(@RequestBody SysDept dept) {
        sysDeptService.create(dept);
        return CommonResult.success();
    }

    /**
     * 更新部门。
     *
     * @param dept 部门信息
     * @return 执行结果
     */
    @PutMapping
    public CommonResult<Void> update(@RequestBody SysDept dept) {
        sysDeptService.update(dept);
        return CommonResult.success();
    }

    /**
     * 删除部门。
     *
     * @param deptId 部门ID
     * @return 执行结果
     */
    @DeleteMapping("/{deptId}")
    public CommonResult<Void> delete(@PathVariable Long deptId) {
        sysDeptService.delete(deptId);
        return CommonResult.success();
    }
}
