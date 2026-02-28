package com.bubua12.atlas.system.service;

import com.bubua12.atlas.system.entity.SysDictData;
import com.bubua12.atlas.system.entity.SysDictType;

import java.util.List;

/**
 * 字典管理服务接口（包含字典类型和字典数据）
 */
public interface SysDictService {

    List<SysDictType> listTypes();

    SysDictType getTypeById(Long dictId);

    void createType(SysDictType dictType);

    void updateType(SysDictType dictType);

    void deleteType(Long dictId);

    List<SysDictData> listDataByType(String dictType);

    SysDictData getDataByCode(Long dictCode);

    void createData(SysDictData dictData);

    void updateData(SysDictData dictData);

    void deleteData(Long dictCode);
}
