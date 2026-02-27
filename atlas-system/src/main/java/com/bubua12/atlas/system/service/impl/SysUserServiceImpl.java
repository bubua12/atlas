package com.bubua12.atlas.system.service.impl;

import com.bubua12.atlas.common.core.domain.PageQuery;
import com.bubua12.atlas.system.entity.SysUser;
import com.bubua12.atlas.system.mapper.SysUserMapper;
import com.bubua12.atlas.system.service.SysUserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl implements SysUserService {

    private final SysUserMapper sysUserMapper;

    @Override
    public IPage<SysUser> list(PageQuery query) {
        Page<SysUser> page = new Page<>(query.getPageNum(), query.getPageSize());
        return sysUserMapper.selectPage(page, null);
    }

    @Override
    public SysUser getById(Long userId) {
        return sysUserMapper.selectById(userId);
    }

    @Override
    public SysUser getByUsername(String username) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        return sysUserMapper.selectOne(wrapper);
    }

    @Override
    public void create(SysUser user) {
        sysUserMapper.insert(user);
    }

    @Override
    public void update(SysUser user) {
        sysUserMapper.updateById(user);
    }

    @Override
    public void delete(Long userId) {
        sysUserMapper.deleteById(userId);
    }
}
