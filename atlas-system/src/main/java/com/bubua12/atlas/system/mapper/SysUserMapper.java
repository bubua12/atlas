package com.bubua12.atlas.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bubua12.atlas.common.mybatis.annotation.DataScope;
import com.bubua12.atlas.system.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户表 Mapper
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 查询用户列表（带数据权限）
     */
    @DataScope(deptAlias = "u", userAlias = "u")
    IPage<SysUser> pageUser(Page<SysUser> page, @Param("username") String username);
}
