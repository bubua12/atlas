package com.bubua12.atlas.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bubua12.atlas.system.mapper.SysDeptMapper;
import com.bubua12.atlas.system.repository.SysDept;
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

    /**
     * 查询部门树。
     *
     * @return 部门树列表
     */
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

    /**
     * 根据部门ID查询详情。
     *
     * @param deptId 部门ID
     * @return 部门信息
     */
    @Override
    public SysDept getById(Long deptId) {
        return sysDeptMapper.selectById(deptId);
    }

    /**
     * 新增部门。
     *
     * @param dept 部门信息
     */
    @Override
    public void create(SysDept dept) {
        sysDeptMapper.insert(dept);
    }

    /**
     * 更新部门。
     *
     * @param dept 部门信息
     */
    @Override
    public void update(SysDept dept) {
        sysDeptMapper.updateById(dept);
    }

    /**
     * 删除部门。
     *
     * @param deptId 部门ID
     */
    @Override
    public void delete(Long deptId) {
        sysDeptMapper.deleteById(deptId);
    }
}
