package com.bubua12.atlas.system.service;

import com.bubua12.atlas.system.entity.SysMenu;

import java.util.List;

/**
 * 菜单管理服务接口
 */
public interface SysMenuService {

    /**
     * 根据用户ID查询权限列表
     */
    List<String> getPermsByUserId(Long userId);

    List<SysMenu> listTree();

    SysMenu getById(Long menuId);

    void create(SysMenu menu);

    void update(SysMenu menu);

    void delete(Long menuId);
}
