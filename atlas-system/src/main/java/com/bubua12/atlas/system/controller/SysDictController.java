package com.bubua12.atlas.system.controller;

import com.bubua12.atlas.common.core.result.CommonResult;
import com.bubua12.atlas.system.entity.SysDictData;
import com.bubua12.atlas.system.entity.SysDictType;
import com.bubua12.atlas.system.service.SysDictService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字典管理控制器（字典类型 + 字典数据）
 */
@RestController
@RequestMapping("/dict")
@RequiredArgsConstructor
public class SysDictController {

    private final SysDictService sysDictService;

    @GetMapping("/type")
    public CommonResult<List<SysDictType>> listTypes() {
        return CommonResult.success(sysDictService.listTypes());
    }

    @GetMapping("/type/{dictId}")
    public CommonResult<SysDictType> getTypeById(@PathVariable Long dictId) {
        return CommonResult.success(sysDictService.getTypeById(dictId));
    }

    @PostMapping("/type")
    public CommonResult<Void> createType(@RequestBody SysDictType dictType) {
        sysDictService.createType(dictType);
        return CommonResult.success();
    }

    @PutMapping("/type")
    public CommonResult<Void> updateType(@RequestBody SysDictType dictType) {
        sysDictService.updateType(dictType);
        return CommonResult.success();
    }

    @DeleteMapping("/type/{dictId}")
    public CommonResult<Void> deleteType(@PathVariable Long dictId) {
        sysDictService.deleteType(dictId);
        return CommonResult.success();
    }

    @GetMapping("/data/{dictType}")
    public CommonResult<List<SysDictData>> listData(@PathVariable String dictType) {
        return CommonResult.success(sysDictService.listDataByType(dictType));
    }

    @GetMapping("/data/code/{dictCode}")
    public CommonResult<SysDictData> getDataByCode(@PathVariable Long dictCode) {
        return CommonResult.success(sysDictService.getDataByCode(dictCode));
    }

    @PostMapping("/data")
    public CommonResult<Void> createData(@RequestBody SysDictData dictData) {
        sysDictService.createData(dictData);
        return CommonResult.success();
    }

    @PutMapping("/data")
    public CommonResult<Void> updateData(@RequestBody SysDictData dictData) {
        sysDictService.updateData(dictData);
        return CommonResult.success();
    }

    @DeleteMapping("/data/{dictCode}")
    public CommonResult<Void> deleteData(@PathVariable Long dictCode) {
        sysDictService.deleteData(dictCode);
        return CommonResult.success();
    }
}
