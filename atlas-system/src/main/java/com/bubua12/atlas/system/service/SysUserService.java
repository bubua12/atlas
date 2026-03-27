package com.bubua12.atlas.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bubua12.atlas.common.core.domain.PageQuery;
import com.bubua12.atlas.system.entity.SysUser;

import java.util.List;

/**
 * 用户管理服务接口
 */
public interface SysUserService {

    IPage<SysUser> list(PageQuery query);

    SysUser getById(Long userId);

    SysUser getByUsername(String username);

    void create(SysUser user);

    void update(SysUser user);

    void delete(Long userId);

    SysUser getByOpenId(String openId);

    SysUser getByPhone(String phone);

    /**
     * 验证用户密码
     */
    boolean verifyPassword(String username, String rawPassword);

    /**
     * 给用户分配角色
     */
    void assignRoles(Long userId, List<Long> roleIds);

    /**
     * 获取用户的角色ID列表
     */
    List<Long> getUserRoleIds(Long userId);

    /**
     * 获取用户的数据权限范围
     */
    Integer getUserDataScope(Long userId);
}
