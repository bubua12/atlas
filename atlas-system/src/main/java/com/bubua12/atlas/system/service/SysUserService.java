package com.bubua12.atlas.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bubua12.atlas.common.core.domain.PageQuery;
import com.bubua12.atlas.system.repository.SysUser;

import java.util.List;

/**
 * 用户管理服务接口
 */
public interface SysUserService {

    /**
     * 分页查询用户列表。
     *
     * @param query 分页参数
     * @return 用户分页结果
     */
    IPage<SysUser> list(PageQuery query);

    /**
     * 根据用户ID查询用户详情。
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    SysUser getById(Long userId);

    /**
     * 根据用户名查询用户。
     *
     * @param username 用户名
     * @return 用户信息
     */
    SysUser getByUsername(String username);

    /**
     * 新增用户。
     *
     * @param user 用户信息
     */
    void create(SysUser user);

    /**
     * 更新用户信息。
     *
     * @param user 用户信息
     */
    void update(SysUser user);

    /**
     * 删除指定用户。
     *
     * @param userId 用户ID
     */
    void delete(Long userId);

    /**
     * 根据 OpenId 查询用户。
     *
     * @param openId 第三方平台唯一标识
     * @return 用户信息
     */
    SysUser getByOpenId(String openId);

    /**
     * 根据手机号查询用户。
     *
     * @param phone 手机号
     * @return 用户信息
     */
    SysUser getByPhone(String phone);

    /**
     * 验证用户密码
     *
     * @param username    用户名
     * @param rawPassword 明文密码
     * @return true 表示校验通过
     */
    boolean verifyPassword(String username, String rawPassword);

    /**
     * 给用户分配角色
     *
     * @param userId  用户ID
     * @param roleIds 角色ID列表
     */
    void assignRoles(Long userId, List<Long> roleIds);

    /**
     * 获取用户的角色ID列表
     *
     * @param userId 用户ID
     * @return 角色ID列表
     */
    List<Long> getUserRoleIds(Long userId);

    /**
     * 获取用户的数据权限范围
     *
     * @param userId 用户ID
     * @return 数据权限值
     */
    Integer getUserDataScope(Long userId);
}
