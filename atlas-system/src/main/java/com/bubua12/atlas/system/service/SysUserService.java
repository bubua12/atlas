package com.bubua12.atlas.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bubua12.atlas.common.core.domain.PageQuery;
import com.bubua12.atlas.system.entity.SysUser;

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
}
