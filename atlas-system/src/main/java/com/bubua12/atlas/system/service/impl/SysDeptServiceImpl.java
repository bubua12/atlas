package com.bubua12.atlas.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bubua12.atlas.system.entity.SysDept;
import com.bubua12.atlas.system.mapper.SysDeptMapper;
import com.bubua12.atlas.system.service.SysDeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 部门管理服务实现（支持树形结构查询）
 */
@Service
@RequiredArgsConstructor
public class SysDeptServiceImpl implements SysDeptService {

    private final SysDeptMapper sysDeptMapper;

    @Override
    public List<SysDept> listTree() {
        LambdaQueryWrapper<SysDept> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(SysDept::getSort);
        List<SysDept> all = sysDeptMapper.selectList(wrapper);
        Map<Long, List<SysDept>> grouped = all.stream()
                .collect(Collectors.groupingBy(SysDept::getParentId));
        all.forEach(d -> d.setChildren(grouped.getOrDefault(d.getDeptId(), new ArrayList<>())));
        return all.stream().filter(d -> d.getParentId() == 0L).collect(Collectors.toList());
    }

    @Override
    public SysDept getById(Long deptId) {
        return sysDeptMapper.selectById(deptId);
    }

    @Override
    public void create(SysDept dept) {
        sysDeptMapper.insert(dept);
    }

    @Override
    public void update(SysDept dept) {
        sysDeptMapper.updateById(dept);
    }

    @Override
    public void delete(Long deptId) {
        sysDeptMapper.deleteById(deptId);
    }
}
