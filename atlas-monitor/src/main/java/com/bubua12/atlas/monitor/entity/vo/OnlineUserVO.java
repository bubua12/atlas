package com.bubua12.atlas.monitor.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 在线用户视图对象
 */
@Data
public class OnlineUserVO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * Token（用于强制下线）
     */
    private String token;

    /**
     * 客户端IP
     */
    private String clientIp;

    /**
     * 登录时间
     */
    private LocalDateTime loginTime;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;
}
