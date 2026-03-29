package com.bubua12.atlas.system.service;

import com.bubua12.atlas.system.repository.SysDept;

import java.util.List;

/**
 * 部门管理服务接口
 */
public interface SysDeptService {

    /**
     * 查询部门树。
     *
     * @return 部门树列表
     */
    List<SysDept> listTree();

    /**
     * 根据部门ID查询详情。
     *
     * @param deptId 部门ID
     * @return 部门信息
     */
    SysDept getById(Long deptId);

    /**
     * 新增部门。
     *
     * @param dept 部门信息
     */
    void create(SysDept dept);

    /**
     * 更新部门。
     *
     * @param dept 部门信息
     */
    void update(SysDept dept);

    /**
     * 删除部门。
     *
     * @param deptId 部门ID
     */
    void delete(Long deptId);
}
