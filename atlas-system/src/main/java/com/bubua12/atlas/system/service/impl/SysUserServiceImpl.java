package com.bubua12.atlas.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bubua12.atlas.common.core.domain.PageQuery;
import com.bubua12.atlas.common.core.utils.PasswordUtils;
import com.bubua12.atlas.system.entity.SysUser;
import com.bubua12.atlas.system.mapper.SysUserMapper;
import com.bubua12.atlas.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 用户管理服务实现
 */
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
        // 加密密码
        if (user.getPassword() != null) {
            user.setPassword(PasswordUtils.encode(user.getPassword()));
        }
        sysUserMapper.insert(user);
    }

    @Override
    public void update(SysUser user) {
        user.setPassword(PasswordUtils.encode(user.getPassword()));
        sysUserMapper.updateById(user);
    }

    @Override
    public void delete(Long userId) {
        sysUserMapper.deleteById(userId);
    }

    @Override
    public SysUser getByOpenId(String openId) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getOpenId, openId);
        return sysUserMapper.selectOne(wrapper);
    }

    @Override
    public SysUser getByPhone(String phone) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getPhone, phone);
        return sysUserMapper.selectOne(wrapper);
    }

    @Override
    public boolean verifyPassword(String username, String rawPassword) {
        SysUser user = getByUsername(username);
        if (user == null) {
            return false;
        }
        return PasswordUtils.matches(rawPassword, user.getPassword());
    }
}
