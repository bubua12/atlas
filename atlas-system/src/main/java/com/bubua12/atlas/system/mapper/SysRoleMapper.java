package com.bubua12.atlas.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bubua12.atlas.system.repository.SysRole;
import com.bubua12.atlas.system.repository.SysUser;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 角色表 Mapper
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    /**
     * 获取角色下绑定了哪些用户
     *
     * @param roleId 角色ID
     * @return 用户列表
     */
    @Select("SELECT u.* FROM sys_user u " +
            "INNER JOIN sys_user_role ur ON u.user_id = ur.user_id " +
            "WHERE ur.role_id = #{roleId} AND u.deleted = 0")
    List<SysUser> selectUsersByRoleId(@Param("roleId") Long roleId);

    /**
     * 删除角色与用户的绑定关系。
     *
     * @param roleId 角色ID
     */
    @Delete("DELETE FROM sys_user_role WHERE role_id = #{roleId}")
    void deleteRoleUsers(@Param("roleId") Long roleId);

    /**
     * 批量新增角色与用户绑定关系。
     *
     * @param roleId 角色ID
     * @param userIds 用户ID列表
     */
    @Insert("<script>" +
            "INSERT INTO sys_user_role (user_id, role_id) VALUES " +
            "<foreach collection='userIds' item='userId' separator=','>" +
            "(#{userId}, #{roleId})" +
            "</foreach>" +
            "</script>")
    void insertRoleUsers(@Param("roleId") Long roleId, @Param("userIds") List<Long> userIds);

    /**
     * 查询角色已绑定的菜单ID列表。
     *
     * @param roleId 角色ID
     * @return 菜单ID列表
     */
    @Select("SELECT menu_id FROM sys_role_menu WHERE role_id = #{roleId}")
    List<Long> selectMenuIdsByRoleId(@Param("roleId") Long roleId);

    /**
     * 删除角色与菜单绑定关系。
     *
     * @param roleId 角色ID
     */
    @Delete("DELETE FROM sys_role_menu WHERE role_id = #{roleId}")
    void deleteRoleMenus(@Param("roleId") Long roleId);

    /**
     * 批量新增角色与菜单绑定关系。
     *
     * @param roleId 角色ID
     * @param menuIds 菜单ID列表
     */
    @Insert("<script>" +
            "INSERT INTO sys_role_menu (role_id, menu_id) VALUES " +
            "<foreach collection='menuIds' item='menuId' separator=','>" +
            "(#{roleId}, #{menuId})" +
            "</foreach>" +
            "</script>")
    void insertRoleMenus(@Param("roleId") Long roleId, @Param("menuIds") List<Long> menuIds);

    /**
     * 删除角色与部门绑定关系。
     *
     * @param roleId 角色ID
     */
    void deleteRoleDepts(@Param("roleId") Long roleId);

    /**
     * 批量新增角色与部门绑定关系。
     *
     * @param roleId 角色ID
     * @param deptIds 部门ID列表
     */
    void insertRoleDepts(@Param("roleId") Long roleId, @Param("deptIds") List<Long> deptIds);
}
