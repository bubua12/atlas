package com.bubua12.atlas.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bubua12.atlas.system.repository.SysDictData;
import com.bubua12.atlas.system.repository.SysDictType;
import com.bubua12.atlas.system.mapper.SysDictDataMapper;
import com.bubua12.atlas.system.mapper.SysDictTypeMapper;
import com.bubua12.atlas.system.service.SysDictService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 字典管理服务实现
 */
@Service
@RequiredArgsConstructor
public class SysDictServiceImpl implements SysDictService {

    private final SysDictTypeMapper sysDictTypeMapper;
    private final SysDictDataMapper sysDictDataMapper;

    /**
     * 查询字典类型列表。
     *
     * @return 字典类型列表
     */
    @Override
    public List<SysDictType> listTypes() {
        return sysDictTypeMapper.selectList(null);
    }

    /**
     * 根据字典类型ID查询详情。
     *
     * @param dictId 字典类型ID
     * @return 字典类型信息
     */
    @Override
    public SysDictType getTypeById(Long dictId) {
        return sysDictTypeMapper.selectById(dictId);
    }

    /**
     * 新增字典类型。
     *
     * @param dictType 字典类型信息
     */
    @Override
    public void createType(SysDictType dictType) {
        sysDictTypeMapper.insert(dictType);
    }

    /**
     * 更新字典类型。
     *
     * @param dictType 字典类型信息
     */
    @Override
    public void updateType(SysDictType dictType) {
        sysDictTypeMapper.updateById(dictType);
    }

    /**
     * 删除字典类型。
     *
     * @param dictId 字典类型ID
     */
    @Override
    public void deleteType(Long dictId) {
        sysDictTypeMapper.deleteById(dictId);
    }

    /**
     * 按字典类型编码查询字典数据列表。
     *
     * @param dictType 字典类型编码
     * @return 字典数据列表
     */
    @Override
    public List<SysDictData> listDataByType(String dictType) {
        LambdaQueryWrapper<SysDictData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDictData::getDictType, dictType)
                .orderByAsc(SysDictData::getDictSort);
        return sysDictDataMapper.selectList(wrapper);
    }

    /**
     * 根据字典数据编码查询详情。
     *
     * @param dictCode 字典数据编码
     * @return 字典数据信息
     */
    @Override
    public SysDictData getDataByCode(Long dictCode) {
        return sysDictDataMapper.selectById(dictCode);
    }

    /**
     * 新增字典数据。
     *
     * @param dictData 字典数据信息
     */
    @Override
    public void createData(SysDictData dictData) {
        sysDictDataMapper.insert(dictData);
    }

    /**
     * 更新字典数据。
     *
     * @param dictData 字典数据信息
     */
    @Override
    public void updateData(SysDictData dictData) {
        sysDictDataMapper.updateById(dictData);
    }

    /**
     * 删除字典数据。
     *
     * @param dictCode 字典数据编码
     */
    @Override
    public void deleteData(Long dictCode) {
        sysDictDataMapper.deleteById(dictCode);
    }
}
