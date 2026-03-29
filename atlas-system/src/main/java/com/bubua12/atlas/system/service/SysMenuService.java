package com.bubua12.atlas.system.service;

import com.bubua12.atlas.system.repository.SysMenu;

import java.util.List;

/**
 * 菜单管理服务接口
 */
public interface SysMenuService {

    /**
     * 根据用户ID查询权限列表
     *
     * @param userId 用户ID
     * @return 权限标识列表
     */
    List<String> getPermsByUserId(Long userId);

    /**
     * 查询菜单树。
     *
     * @return 菜单树列表
     */
    List<SysMenu> listTree();

    /**
     * 根据菜单ID查询详情。
     *
     * @param menuId 菜单ID
     * @return 菜单信息
     */
    SysMenu getById(Long menuId);

    /**
     * 新增菜单。
     *
     * @param menu 菜单信息
     */
    void create(SysMenu menu);

    /**
     * 更新菜单。
     *
     * @param menu 菜单信息
     */
    void update(SysMenu menu);

    /**
     * 删除菜单。
     *
     * @param menuId 菜单ID
     */
    void delete(Long menuId);
}
