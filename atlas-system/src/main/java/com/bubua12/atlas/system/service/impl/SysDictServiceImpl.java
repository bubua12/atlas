package com.bubua12.atlas.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bubua12.atlas.system.entity.SysDictData;
import com.bubua12.atlas.system.entity.SysDictType;
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

    @Override
    public List<SysDictType> listTypes() {
        return sysDictTypeMapper.selectList(null);
    }

    @Override
    public SysDictType getTypeById(Long dictId) {
        return sysDictTypeMapper.selectById(dictId);
    }

    @Override
    public void createType(SysDictType dictType) {
        sysDictTypeMapper.insert(dictType);
    }

    @Override
    public void updateType(SysDictType dictType) {
        sysDictTypeMapper.updateById(dictType);
    }

    @Override
    public void deleteType(Long dictId) {
        sysDictTypeMapper.deleteById(dictId);
    }

    @Override
    public List<SysDictData> listDataByType(String dictType) {
        LambdaQueryWrapper<SysDictData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDictData::getDictType, dictType)
                .orderByAsc(SysDictData::getDictSort);
        return sysDictDataMapper.selectList(wrapper);
    }

    @Override
    public SysDictData getDataByCode(Long dictCode) {
        return sysDictDataMapper.selectById(dictCode);
    }

    @Override
    public void createData(SysDictData dictData) {
        sysDictDataMapper.insert(dictData);
    }

    @Override
    public void updateData(SysDictData dictData) {
        sysDictDataMapper.updateById(dictData);
    }

    @Override
    public void deleteData(Long dictCode) {
        sysDictDataMapper.deleteById(dictCode);
    }
}
