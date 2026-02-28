package com.bubua12.atlas.api.system.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * 用户数据传输对象
 * 跨服务传递用户信息的 DTO，由 atlas-system 服务通过 Feign 返回。
 */
@Data
public class UserDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 用户名（登录账号）
     */
    private String username;
    /**
     * 昵称
     */
    private String nickname;
    /**
     * 密码（密文）
     */
    private String password;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 手机号
     */
    private String phone;
    /**
     * 账号状态（0正常 1停用）
     */
    private Integer status;
    /**
     * 部门ID
     */
    private Long deptId;
    /**
     * 角色ID列表
     */
    private List<Long> roleIds;
    /**
     * 权限标识集合
     */
    private Set<String> permissions;
}
