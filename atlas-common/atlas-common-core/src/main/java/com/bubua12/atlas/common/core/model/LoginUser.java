package com.bubua12.atlas.common.core.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

/**
 * 登录用户模型
 * 认证通过后缓存到 Redis 的用户会话信息，用于网关鉴权。
 */
@Data
public class LoginUser implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 客户端IP
     */
    private String clientIp;

    /**
     * 当前会话的 JWT 令牌
     */
    private String token;

    /**
     * 用户权限标识集合
     */
    private Set<String> permissions;
}
