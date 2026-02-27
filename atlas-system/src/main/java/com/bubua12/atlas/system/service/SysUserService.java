package com.bubua12.atlas.system.service;

import com.bubua12.atlas.common.core.domain.PageQuery;
import com.bubua12.atlas.system.entity.SysUser;
import com.baomidou.mybatisplus.core.metadata.IPage;

public interface SysUserService {

    IPage<SysUser> list(PageQuery query);

    SysUser getById(Long userId);

    SysUser getByUsername(String username);

    void create(SysUser user);

    void update(SysUser user);

    void delete(Long userId);
}
