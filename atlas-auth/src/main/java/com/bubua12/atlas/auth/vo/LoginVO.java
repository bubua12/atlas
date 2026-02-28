package com.bubua12.atlas.auth.vo;

import lombok.Data;

/**
 * 登录响应视图对象
 * 登录成功后返回给前端的数据结构。
 */
@Data
public class LoginVO {

    /**
     * JWT 访问令牌
     */
    private String token;

    /**
     * 令牌过期时间（秒）
     */
    private Long expiresIn;
}
