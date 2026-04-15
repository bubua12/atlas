package com.bubua12.atlas.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bubua12.atlas.common.mybatis.annotation.DataScope;
import com.bubua12.atlas.system.repository.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户表 Mapper
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 查询用户列表（带数据权限）
     *
     * @param page     分页参数
     * @param username 用户名关键字
     * @return 用户分页结果
     */
    @DataScope(deptAlias = "u", userAlias = "u")
    IPage<SysUser> pageUser(Page<SysUser> page, @Param("username") String username);

    /**
     * 删除用户已绑定的角色
     *
     * @param userId 用户ID
     */
    void deleteUserRoles(@Param("userId") Long userId);

    /**
     * 给用户绑定角色
     *
     * @param userId  用户ID
     * @param roleIds 角色ID列表
     */
    void insertUserRoles(@Param("userId") Long userId, @Param("roleIds") List<Long> roleIds);

    /**
     * 获取用户的角色ID列表
     *
     * @param userId 用户ID
     * @return 角色ID列表
     */
    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);

    /**
     * 根据用户id获取用户的数据权限
     *
     * @param userId 用户ID
     * @return 数据权限值
     */
    Integer selectUserDataScope(@Param("userId") Long userId);
}
