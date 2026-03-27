package com.bubua12.atlas.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bubua12.atlas.system.entity.SysRole;
import com.bubua12.atlas.system.entity.SysUser;
import com.bubua12.atlas.system.mapper.SysRoleMapper;
import com.bubua12.atlas.system.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 角色管理服务实现
 */
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl implements SysRoleService {

    private final SysRoleMapper sysRoleMapper;

    @Override
    public List<SysRole> list() {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(SysRole::getSort);
        return sysRoleMapper.selectList(wrapper);
    }

    @Override
    public SysRole getById(Long roleId) {
        return sysRoleMapper.selectById(roleId);
    }

    @Override
    public void create(SysRole role) {
        sysRoleMapper.insert(role);
    }

    @Override
    public void update(SysRole role) {
        sysRoleMapper.updateById(role);
    }

    @Override
    public void delete(Long roleId) {
        sysRoleMapper.deleteById(roleId);
    }

    @Override
    public List<SysUser> getRoleUsers(Long roleId) {
        return sysRoleMapper.selectUsersByRoleId(roleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignUsers(Long roleId, List<Long> userIds) {
        sysRoleMapper.deleteRoleUsers(roleId);
        if (userIds != null && !userIds.isEmpty()) {
            sysRoleMapper.insertRoleUsers(roleId, userIds);
        }
    }

    @Override
    public List<Long> getRoleMenuIds(Long roleId) {
        return sysRoleMapper.selectMenuIdsByRoleId(roleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignMenus(Long roleId, List<Long> menuIds) {
        sysRoleMapper.deleteRoleMenus(roleId);
        if (menuIds != null && !menuIds.isEmpty()) {
            sysRoleMapper.insertRoleMenus(roleId, menuIds);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDataScope(Long roleId, Integer dataScope, List<Long> deptIds) {
        SysRole role = new SysRole();
        role.setRoleId(roleId);
        role.setDataScope(dataScope);
        sysRoleMapper.updateById(role);

        sysRoleMapper.deleteRoleDepts(roleId);
        if (dataScope == 5 && deptIds != null && !deptIds.isEmpty()) {
            sysRoleMapper.insertRoleDepts(roleId, deptIds);
        }
    }
}
