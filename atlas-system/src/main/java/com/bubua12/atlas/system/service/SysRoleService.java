package com.bubua12.atlas.system.service;

import com.bubua12.atlas.system.entity.SysRole;

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
}
