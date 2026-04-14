package com.bubua12.atlas.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bubua12.atlas.common.core.domain.PageQuery;
import com.bubua12.atlas.common.core.utils.PasswordUtils;
import com.bubua12.atlas.common.log.annotation.OperLog;
import com.bubua12.atlas.system.security.PermissionChangePublisher;
import com.bubua12.atlas.system.repository.SysUser;
import com.bubua12.atlas.system.mapper.SysUserMapper;
import com.bubua12.atlas.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户管理服务实现
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl implements SysUserService {

    private final SysUserMapper sysUserMapper;

    private final PermissionChangePublisher permissionChangePublisher;

    /**
     * 分页查询用户列表，查询过程中会应用数据权限过滤。
     *
     * @param query 分页参数
     * @return 用户分页结果
     */
    @Override
    public IPage<SysUser> list(PageQuery query) {
        Page<SysUser> page = new Page<>(query.getPageNum(), query.getPageSize());
        // 使用带数据权限的查询方法
        return sysUserMapper.pageUser(page, null);
    }

    /**
     * 根据用户ID查询用户详情。
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    @Override
    public SysUser getById(Long userId) {
        return sysUserMapper.selectById(userId);
    }

    /**
     * 根据用户名查询用户。
     *
     * @param username 用户名
     * @return 用户信息
     */
    @Override
    public SysUser getByUsername(String username) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        return sysUserMapper.selectOne(wrapper);
    }

    /**
     * 新增用户，并在入库前对密码进行加密。
     *
     * @param user 用户信息
     */
    @Override
    public void create(SysUser user) {
        // 加密密码
        if (user.getPassword() != null) {
            user.setPassword(PasswordUtils.encode(user.getPassword()));
        }
        sysUserMapper.insert(user);
    }

    /**
     * 更新用户信息，并在更新前对密码进行加密。
     *
     * @param user 用户信息
     */
    @Override
    @OperLog(title = "修改用户", businessType = "修改")
    public void update(SysUser user) {
        // 只有当密码字段不为空且不是已加密格式时才加密
        if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
            user.setPassword(PasswordUtils.encode(user.getPassword()));
        }
        sysUserMapper.updateById(user);
    }

    /**
     * 按用户ID删除用户。
     *
     * @param userId 用户ID
     */
    @Override
    public void delete(Long userId) {
        sysUserMapper.deleteById(userId);
    }

    /**
     * 根据 OpenId 查询用户。
     *
     * @param openId 第三方平台唯一标识
     * @return 用户信息
     */
    @Override
    public SysUser getByOpenId(String openId) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getOpenId, openId);
        return sysUserMapper.selectOne(wrapper);
    }

    /**
     * 根据手机号查询用户。
     *
     * @param phone 手机号
     * @return 用户信息
     */
    @Override
    public SysUser getByPhone(String phone) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getPhone, phone);
        return sysUserMapper.selectOne(wrapper);
    }

    /**
     * 校验用户密码。
     *
     * @param username 用户名
     * @param rawPassword 明文密码
     * @return true 表示密码匹配
     */
    @Override
    public boolean verifyPassword(String username, String rawPassword) {
        SysUser user = getByUsername(username);
        if (user == null) {
            return false;
        }
        return PasswordUtils.matches(rawPassword, user.getPassword());
    }

    /**
     * 给用户分配角色
     *
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(Long userId, List<Long> roleIds) {
        // 1、根据用户ID删除用户角色表的关系
        sysUserMapper.deleteUserRoles(userId);
        // 2、配置新用户角色关系
        if (CollectionUtils.isNotEmpty(roleIds)) {
            sysUserMapper.insertUserRoles(userId, roleIds);
        }
        permissionChangePublisher.publishUsersChanged(List.of(userId), "user-role-changed");
    }

    /**
     * 获取用户的角色ID列表
     *
     * @param userId 用户ID
     * @return 角色ID列表
     */
    @Override
    public List<Long> getUserRoleIds(Long userId) {
        return sysUserMapper.selectRoleIdsByUserId(userId);
    }

    /**
     * 获取用户的数据权限
     *
     * @param userId 用户ID
     * @return 数据权限值
     */
    @Override
    public Integer getUserDataScope(Long userId) {
        return sysUserMapper.selectUserDataScope(userId);
    }
}
