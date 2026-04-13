package com.bubua12.atlas.common.redis.pubsub;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 权限变更事件发布器
 * 权限或角色变更时，通知 auth 服务清除相关 Token 缓存
 */
@Component
@RequiredArgsConstructor
public class PermissionChangePublisher {

    private final RedisTemplate<String, Object> redisTemplate;

    public static final String CHANNEL = "permission:change";

    public void publishUserPermissionChange(Long userId) {
        redisTemplate.convertAndSend(CHANNEL, Map.of(
            "type", "user",
            "userId", userId
        ));
    }

    public void publishRolePermissionChange(Long roleId) {
        redisTemplate.convertAndSend(CHANNEL, Map.of(
            "type", "role",
            "roleId", roleId
        ));
    }
}
