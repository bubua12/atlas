package com.bubua12.atlas.system.service;

import com.bubua12.atlas.system.entity.SysDept;

import java.util.List;

/**
 * 部门管理服务接口
 */
public interface SysDeptService {

    List<SysDept> listTree();

    SysDept getById(Long deptId);

    void create(SysDept dept);

    void update(SysDept dept);

    void delete(Long deptId);
}
