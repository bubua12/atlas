package com.bubua12.atlas.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bubua12.atlas.api.auth.constant.AuthCacheConstant;
import com.bubua12.atlas.system.mapper.SysMenuMapper;
import com.bubua12.atlas.system.mapper.SysRoleMapper;
import com.bubua12.atlas.system.repository.SysRole;
import com.bubua12.atlas.system.repository.SysUser;
import com.bubua12.atlas.system.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 角色管理服务实现
 */
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl implements SysRoleService {

    private final SysRoleMapper sysRoleMapper;
    private final SysMenuMapper sysMenuMapper;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 查询角色列表。
     *
     * @return 角色列表
     */
    @Override
    public List<SysRole> list() {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(SysRole::getSort);
        return sysRoleMapper.selectList(wrapper);
    }

    /**
     * 根据角色ID查询详情。
     *
     * @param roleId 角色ID
     * @return 角色信息
     */
    @Override
    public SysRole getById(Long roleId) {
        return sysRoleMapper.selectById(roleId);
    }

    /**
     * 新增角色。
     *
     * @param role 角色信息
     */
    @Override
    public void create(SysRole role) {
        sysRoleMapper.insert(role);
    }

    /**
     * 更新角色。
     *
     * @param role 角色信息
     */
    @Override
    public void update(SysRole role) {
        sysRoleMapper.updateById(role);
    }

    /**
     * 删除角色。
     *
     * @param roleId 角色ID
     */
    @Override
    public void delete(Long roleId) {
        sysRoleMapper.deleteById(roleId);
    }

    /**
     * 查询角色下已绑定的用户列表。
     *
     * @param roleId 角色ID
     * @return 用户列表
     */
    @Override
    public List<SysUser> getRoleUsers(Long roleId) {
        return sysRoleMapper.selectUsersByRoleId(roleId);
    }

    /**
     * 给角色分配用户
     *
     * @param roleId  角色ID
     * @param userIds 用户ID列表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignUsers(Long roleId, List<Long> userIds) {
        // 1、先根据角色ID删除旧的关系
        sysRoleMapper.deleteRoleUsers(roleId);
        // 2、绑定新的关系
        if (CollectionUtils.isNotEmpty(userIds)) {
            sysRoleMapper.insertRoleUsers(roleId, userIds);
        }
    }

    /**
     * 查询角色已绑定的菜单ID列表。
     *
     * @param roleId 角色ID
     * @return 菜单ID列表
     */
    @Override
    public List<Long> getRoleMenuIds(Long roleId) {
        return sysRoleMapper.selectMenuIdsByRoleId(roleId);
    }

    /**
     * 给角色分配菜单。
     *
     * @param roleId  角色ID
     * @param menuIds 菜单ID列表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignMenus(Long roleId, List<Long> menuIds) {
        // 1、先根据角色id删除旧的和菜单绑定的关系
        sysRoleMapper.deleteRoleMenus(roleId);
        // 2、给角色绑定新的菜单
        if (CollectionUtils.isNotEmpty(menuIds)) {
            sysRoleMapper.insertRoleMenus(roleId, menuIds);
        }
        publishRoleUsersRefresh(roleId);
    }

    /**
     * 更新角色数据权限。
     *
     * @param roleId    角色ID
     * @param dataScope 数据权限值
     * @param deptIds   部门ID列表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDataScope(Long roleId, Integer dataScope, List<Long> deptIds) {
        SysRole role = new SysRole();
        role.setRoleId(roleId);
        role.setDataScope(dataScope);
        sysRoleMapper.updateById(role);

        sysRoleMapper.deleteRoleDepts(roleId);
        if (dataScope == 5 && deptIds != null && !deptIds.isEmpty()) {
            sysRoleMapper.insertRoleDepts(roleId, deptIds);
        }
        publishRoleUsersRefresh(roleId);
    }

    /**
     * 角色菜单或数据权限一旦变化，角色下所有已登录用户都要同步刷新登录态。
     */
    private void publishRoleUsersRefresh(Long roleId) {
        // fixme 这里使用缓存好不好？直接走MYSQL的影响好不好？这里容易形成性能瓶颈，做成缓存？但是感觉变化比较多不太合适
        List<SysUser> users = sysRoleMapper.selectUsersByRoleId(roleId);
        for (SysUser user : users) {
            List<String> perms = sysMenuMapper.selectPermsByUserId(user.getUserId());
            String permsStr = String.join(",", perms);
            // 登录态里存的是用户当前生效的数据权限，因此这里按用户聚合后的最终 dataScope 回填，而不是直接复用当前 roleId 的值。
            int userDataScope = user.getUserId() != null && user.getUserId() == 1L ? 1 : getUserDataScope(user.getUserId());
            stringRedisTemplate.convertAndSend(AuthCacheConstant.PERMISSION_CHANGE_CHANNEL,
                    "user:" + user.getUserId() + ":" + userDataScope + ":"
                            + (user.getDeptId() == null ? "null" : user.getDeptId()) + ":" + permsStr
            );
        }
    }

    private Integer getUserDataScope(Long userId) {
        List<SysRole> roles = sysRoleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                .inSql(SysRole::getRoleId, "select role_id from sys_user_role where user_id = " + userId)
                .orderByAsc(SysRole::getDataScope));
        return roles.isEmpty() ? 4 : roles.get(0).getDataScope();
    }
}
