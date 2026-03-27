package com.bubua12.atlas.system.service;

import com.bubua12.atlas.system.entity.SysRole;
import com.bubua12.atlas.system.entity.SysUser;

import java.util.List;

/**
 * 角色管理服务接口
 */
public interface SysRoleService {

    List<SysRole> list();

    SysRole getById(Long roleId);

    void create(SysRole role);

    void update(SysRole role);

    void delete(Long roleId);

    /**
     * 获取角色下的用户列表
     */
    List<SysUser> getRoleUsers(Long roleId);

    /**
     * 给角色分配用户
     */
    void assignUsers(Long roleId, List<Long> userIds);

    /**
     * 获取角色的菜单ID列表
     */
    List<Long> getRoleMenuIds(Long roleId);

    /**
     * 给角色分配菜单
     */
    void assignMenus(Long roleId, List<Long> menuIds);

    /**
     * 更新角色数据权限
     */
    void updateDataScope(Long roleId, Integer dataScope, List<Long> deptIds);
}
