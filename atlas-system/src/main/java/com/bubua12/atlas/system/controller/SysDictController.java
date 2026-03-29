package com.bubua12.atlas.system.controller;

import com.bubua12.atlas.common.core.result.CommonResult;
import com.bubua12.atlas.system.repository.SysDictData;
import com.bubua12.atlas.system.repository.SysDictType;
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

    /**
     * 查询字典类型列表。
     *
     * @return 字典类型列表
     */
    @GetMapping("/type")
    public CommonResult<List<SysDictType>> listTypes() {
        return CommonResult.success(sysDictService.listTypes());
    }

    /**
     * 根据字典类型ID查询详情。
     *
     * @param dictId 字典类型ID
     * @return 字典类型详情
     */
    @GetMapping("/type/{dictId}")
    public CommonResult<SysDictType> getTypeById(@PathVariable Long dictId) {
        return CommonResult.success(sysDictService.getTypeById(dictId));
    }

    /**
     * 新增字典类型。
     *
     * @param dictType 字典类型信息
     * @return 执行结果
     */
    @PostMapping("/type")
    public CommonResult<Void> createType(@RequestBody SysDictType dictType) {
        sysDictService.createType(dictType);
        return CommonResult.success();
    }

    /**
     * 更新字典类型。
     *
     * @param dictType 字典类型信息
     * @return 执行结果
     */
    @PutMapping("/type")
    public CommonResult<Void> updateType(@RequestBody SysDictType dictType) {
        sysDictService.updateType(dictType);
        return CommonResult.success();
    }

    /**
     * 删除字典类型。
     *
     * @param dictId 字典类型ID
     * @return 执行结果
     */
    @DeleteMapping("/type/{dictId}")
    public CommonResult<Void> deleteType(@PathVariable Long dictId) {
        sysDictService.deleteType(dictId);
        return CommonResult.success();
    }

    /**
     * 根据字典类型编码查询字典数据列表。
     *
     * @param dictType 字典类型编码
     * @return 字典数据列表
     */
    @GetMapping("/data/{dictType}")
    public CommonResult<List<SysDictData>> listData(@PathVariable String dictType) {
        return CommonResult.success(sysDictService.listDataByType(dictType));
    }

    /**
     * 根据字典数据编码查询详情。
     *
     * @param dictCode 字典数据编码
     * @return 字典数据详情
     */
    @GetMapping("/data/code/{dictCode}")
    public CommonResult<SysDictData> getDataByCode(@PathVariable Long dictCode) {
        return CommonResult.success(sysDictService.getDataByCode(dictCode));
    }

    /**
     * 新增字典数据。
     *
     * @param dictData 字典数据信息
     * @return 执行结果
     */
    @PostMapping("/data")
    public CommonResult<Void> createData(@RequestBody SysDictData dictData) {
        sysDictService.createData(dictData);
        return CommonResult.success();
    }

    /**
     * 更新字典数据。
     *
     * @param dictData 字典数据信息
     * @return 执行结果
     */
    @PutMapping("/data")
    public CommonResult<Void> updateData(@RequestBody SysDictData dictData) {
        sysDictService.updateData(dictData);
        return CommonResult.success();
    }

    /**
     * 删除字典数据。
     *
     * @param dictCode 字典数据编码
     * @return 执行结果
     */
    @DeleteMapping("/data/{dictCode}")
    public CommonResult<Void> deleteData(@PathVariable Long dictCode) {
        sysDictService.deleteData(dictCode);
        return CommonResult.success();
    }
}
