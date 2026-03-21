package com.bubua12.atlas.auth.entity.request;

import lombok.Data;

/**
 * 统一登录请求体
 * 前端通过 grantType 字段指定登录方式，不同方式携带不同参数：
 * - password: username + password
 * - captcha:  phone + captchaCode + captchaKey
 * - wechat:   wxCode
 */
@Data
public class LoginRequest {

    /**
     * 授权类型，用于路由到对应的 LoginHandler（如 password / captcha / wechat）
     */
    private String grantType;

    /**
     * 用户名（密码登录时使用）
     */
    private String username;

    /**
     * 密码（密码登录时使用）
     */
    private String password;

    /**
     * 手机号（验证码登录时使用）
     */
    private String phone;

    /**
     * 短信验证码（验证码登录时使用）
     */
    private String captchaCode;

    /**
     * 验证码标识key（验证码登录时使用）
     */
    private String captchaKey;

    /**
     * 微信授权码（微信登录时使用）
     */
    private String wxCode;

    /**
     * 客户端 IP 地址（用于防暴力破解）
     */
    private String clientIp;
}
