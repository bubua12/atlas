package com.bubua12.atlas.system.service;

import com.bubua12.atlas.system.repository.SysDictData;
import com.bubua12.atlas.system.repository.SysDictType;

import java.util.List;

/**
 * 字典管理服务接口（包含字典类型和字典数据）
 */
public interface SysDictService {

    /**
     * 查询字典类型列表。
     *
     * @return 字典类型列表
     */
    List<SysDictType> listTypes();

    /**
     * 根据字典类型ID查询详情。
     *
     * @param dictId 字典类型ID
     * @return 字典类型信息
     */
    SysDictType getTypeById(Long dictId);

    /**
     * 新增字典类型。
     *
     * @param dictType 字典类型信息
     */
    void createType(SysDictType dictType);

    /**
     * 更新字典类型。
     *
     * @param dictType 字典类型信息
     */
    void updateType(SysDictType dictType);

    /**
     * 删除字典类型。
     *
     * @param dictId 字典类型ID
     */
    void deleteType(Long dictId);

    /**
     * 按字典类型编码查询字典数据列表。
     *
     * @param dictType 字典类型编码
     * @return 字典数据列表
     */
    List<SysDictData> listDataByType(String dictType);

    /**
     * 根据字典数据编码查询详情。
     *
     * @param dictCode 字典数据编码
     * @return 字典数据信息
     */
    SysDictData getDataByCode(Long dictCode);

    /**
     * 新增字典数据。
     *
     * @param dictData 字典数据信息
     */
    void createData(SysDictData dictData);

    /**
     * 更新字典数据。
     *
     * @param dictData 字典数据信息
     */
    void updateData(SysDictData dictData);

    /**
     * 删除字典数据。
     *
     * @param dictCode 字典数据编码
     */
    void deleteData(Long dictCode);
}
