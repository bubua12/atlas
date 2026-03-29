package com.bubua12.atlas.system.service;

import com.bubua12.atlas.system.repository.SysRole;
import com.bubua12.atlas.system.repository.SysUser;

import java.util.List;

/**
 * 角色管理服务接口
 */
public interface SysRoleService {

    /**
     * 查询角色列表。
     *
     * @return 角色列表
     */
    List<SysRole> list();

    /**
     * 根据角色ID查询详情。
     *
     * @param roleId 角色ID
     * @return 角色信息
     */
    SysRole getById(Long roleId);

    /**
     * 新增角色。
     *
     * @param role 角色信息
     */
    void create(SysRole role);

    /**
     * 更新角色。
     *
     * @param role 角色信息
     */
    void update(SysRole role);

    /**
     * 删除角色。
     *
     * @param roleId 角色ID
     */
    void delete(Long roleId);

    /**
     * 获取角色下的用户列表
     *
     * @param roleId 角色ID
     * @return 用户列表
     */
    List<SysUser> getRoleUsers(Long roleId);

    /**
     * 给角色分配用户
     *
     * @param roleId 角色ID
     * @param userIds 用户ID列表
     */
    void assignUsers(Long roleId, List<Long> userIds);

    /**
     * 获取角色已经绑定了的菜单权限
     *
     * @param roleId 角色ID
     * @return 菜单ID列表
     */
    List<Long> getRoleMenuIds(Long roleId);

    /**
     * 给角色分配菜单
     *
     * @param roleId 角色ID
     * @param menuIds 菜单ID列表
     */
    void assignMenus(Long roleId, List<Long> menuIds);

    /**
     * 更新角色数据权限
     *
     * @param roleId 角色ID
     * @param dataScope 数据权限值
     * @param deptIds 部门ID列表
     */
    void updateDataScope(Long roleId, Integer dataScope, List<Long> deptIds);
}
