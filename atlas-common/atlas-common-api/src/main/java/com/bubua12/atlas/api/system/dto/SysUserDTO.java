package com.bubua12.atlas.api.system.dto;

import lombok.Data;

/**
 *
 *
 * @author bubua12
 * @since 2026/03/15 23:23
 */
@Data
public class SysUserDTO {

    /**
     * 用户账号
     */
    private String username;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 微信OpenID
     */
    private String openId;

    /**
     * 密码 BCrypt加密
     */
    private String password;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 性别（0未知 1男 2女）
     */
    private Integer sex;


    /**
     * 状态（0正常 1停用）
     */
    private Integer status;

    /**
     * 所属部门ID
     */
    private Long deptId;

    /**
     * 创建者
     */
    private String createBy;
}
