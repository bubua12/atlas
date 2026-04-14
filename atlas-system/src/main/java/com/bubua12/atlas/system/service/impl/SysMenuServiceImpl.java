package com.bubua12.atlas.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bubua12.atlas.common.security.constant.PermissionConstants;
import com.bubua12.atlas.system.security.PermissionChangePublisher;
import com.bubua12.atlas.system.repository.SysMenu;
import com.bubua12.atlas.system.mapper.SysMenuMapper;
import com.bubua12.atlas.system.mapper.SysRoleMapper;
import com.bubua12.atlas.system.service.SysMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 菜单管理服务实现（支持树形结构查询）
 */
@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl implements SysMenuService {

    private final SysMenuMapper sysMenuMapper;

    private final SysRoleMapper sysRoleMapper;

    private final PermissionChangePublisher permissionChangePublisher;

    /**
     * 根据用户ID查询权限标识列表。
     *
     * @param userId 用户ID
     * @return 权限标识列表
     */
    @Override
    public List<String> getPermsByUserId(Long userId) {
        // 管理员拥有所有权限
        if (Long.valueOf(1L).equals(userId)) {
            return List.of(PermissionConstants.ALL_PERMISSION);
        }
        return sysMenuMapper.selectPermsByUserId(userId);
    }

    /**
     * 查询菜单树。
     *
     * @return 菜单树列表
     */
    @Override
    public List<SysMenu> listTree() {
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(SysMenu::getSort);
        List<SysMenu> all = sysMenuMapper.selectList(wrapper);
        return buildTree(all);
    }

    /**
     * 根据菜单ID查询详情。
     *
     * @param menuId 菜单ID
     * @return 菜单信息
     */
    @Override
    public SysMenu getById(Long menuId) {
        return sysMenuMapper.selectById(menuId);
    }

    /**
     * 新增菜单。
     *
     * @param menu 菜单信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(SysMenu menu) {
        sysMenuMapper.insert(menu);
    }

    /**
     * 更新菜单。
     *
     * @param menu 菜单信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SysMenu menu) {
        List<Long> affectedUserIds = sysRoleMapper.selectUserIdsByMenuId(menu.getMenuId());
        sysMenuMapper.updateById(menu);
        permissionChangePublisher.publishUsersChanged(affectedUserIds, "menu-updated");
    }

    /**
     * 删除菜单。
     *
     * @param menuId 菜单ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long menuId) {
        List<Long> affectedUserIds = sysRoleMapper.selectUserIdsByMenuId(menuId);
        sysMenuMapper.deleteById(menuId);
        permissionChangePublisher.publishUsersChanged(affectedUserIds, "menu-deleted");
    }

    /**
     * 构建树形菜单结构。
     *
     * @param all 扁平菜单列表
     * @return 树形菜单列表
     */
    private List<SysMenu> buildTree(List<SysMenu> all) {
        Map<Long, List<SysMenu>> grouped = all.stream()
                .collect(Collectors.groupingBy(SysMenu::getParentId));
        all.forEach(m -> m.setChildren(grouped.getOrDefault(m.getMenuId(), new ArrayList<>())));
        return all.stream().filter(m -> m.getParentId() == 0L).collect(Collectors.toList());
    }
}
