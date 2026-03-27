package com.bubua12.atlas.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bubua12.atlas.system.entity.SysRole;
import com.bubua12.atlas.system.entity.SysUser;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色表 Mapper
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    @Select("SELECT u.* FROM sys_user u " +
            "INNER JOIN sys_user_role ur ON u.user_id = ur.user_id " +
            "WHERE ur.role_id = #{roleId} AND u.deleted = 0")
    List<SysUser> selectUsersByRoleId(@Param("roleId") Long roleId);

    @Delete("DELETE FROM sys_user_role WHERE role_id = #{roleId}")
    void deleteRoleUsers(@Param("roleId") Long roleId);

    @Insert("<script>" +
            "INSERT INTO sys_user_role (user_id, role_id) VALUES " +
            "<foreach collection='userIds' item='userId' separator=','>" +
            "(#{userId}, #{roleId})" +
            "</foreach>" +
            "</script>")
    void insertRoleUsers(@Param("roleId") Long roleId, @Param("userIds") List<Long> userIds);

    @Select("SELECT menu_id FROM sys_role_menu WHERE role_id = #{roleId}")
    List<Long> selectMenuIdsByRoleId(@Param("roleId") Long roleId);

    @Delete("DELETE FROM sys_role_menu WHERE role_id = #{roleId}")
    void deleteRoleMenus(@Param("roleId") Long roleId);

    @Insert("<script>" +
            "INSERT INTO sys_role_menu (role_id, menu_id) VALUES " +
            "<foreach collection='menuIds' item='menuId' separator=','>" +
            "(#{roleId}, #{menuId})" +
            "</foreach>" +
            "</script>")
    void insertRoleMenus(@Param("roleId") Long roleId, @Param("menuIds") List<Long> menuIds);

    void deleteRoleDepts(@Param("roleId") Long roleId);

    void insertRoleDepts(@Param("roleId") Long roleId, @Param("deptIds") List<Long> deptIds);
}
