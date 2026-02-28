package com.bubua12.atlas.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bubua12.atlas.system.entity.SysMenu;
import com.bubua12.atlas.system.mapper.SysMenuMapper;
import com.bubua12.atlas.system.service.SysMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    @Override
    public List<SysMenu> listTree() {
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(SysMenu::getSort);
        List<SysMenu> all = sysMenuMapper.selectList(wrapper);
        return buildTree(all);
    }

    @Override
    public SysMenu getById(Long menuId) {
        return sysMenuMapper.selectById(menuId);
    }

    @Override
    public void create(SysMenu menu) {
        sysMenuMapper.insert(menu);
    }

    @Override
    public void update(SysMenu menu) {
        sysMenuMapper.updateById(menu);
    }

    @Override
    public void delete(Long menuId) {
        sysMenuMapper.deleteById(menuId);
    }

    private List<SysMenu> buildTree(List<SysMenu> all) {
        Map<Long, List<SysMenu>> grouped = all.stream()
                .collect(Collectors.groupingBy(SysMenu::getParentId));
        all.forEach(m -> m.setChildren(grouped.getOrDefault(m.getMenuId(), new ArrayList<>())));
        return all.stream().filter(m -> m.getParentId() == 0L).collect(Collectors.toList());
    }
}
