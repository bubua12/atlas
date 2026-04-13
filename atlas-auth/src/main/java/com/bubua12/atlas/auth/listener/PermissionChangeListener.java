package com.bubua12.atlas.auth.listener;

import com.bubua12.atlas.api.auth.constant.AuthCacheConstant;
import com.bubua12.atlas.common.core.model.LoginUser;
import com.bubua12.atlas.common.redis.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * 权限变更监听器
 * 订阅 permission:change 频道，清除受影响用户的 Token 缓存
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PermissionChangeListener implements MessageListener {

    private final RedisService redisService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            Object body = redisTemplate.getValueSerializer().deserialize(message.getBody());
            if (!(body instanceof Map<?, ?> msg)) return;

            String type = (String) msg.get("type");
            if ("user".equals(type)) {
                Long userId = ((Number) msg.get("userId")).longValue();
                clearUserTokens(userId);
            } else if ("role".equals(type)) {
                Long roleId = ((Number) msg.get("roleId")).longValue();
                clearTokensByRole(roleId);
            }
        } catch (Exception e) {
            log.error("处理权限变更消息失败", e);
        }
    }

    private void clearUserTokens(Long userId) {
        Set<String> keys = redisService.keys(AuthCacheConstant.AUTH_TOKEN_CACHE_PREFIX + "*");
        if (keys == null) return;
        for (String key : keys) {
            LoginUser user = redisService.get(key);
            if (user != null && userId.equals(user.getUserId())) {
                redisService.delete(key);
                log.info("清除用户 {} 的 Token 缓存: {}", userId, key);
            }
        }
    }

    private void clearTokensByRole(Long roleId) {
        // LoginUser 未存储 roleIds，角色权限变更时清除所有 Token（强制重新登录）
        Set<String> keys = redisService.keys(AuthCacheConstant.AUTH_TOKEN_CACHE_PREFIX + "*");
        if (keys == null) return;
        keys.forEach(redisService::delete);
        log.info("角色 {} 权限变更，已清除所有 Token 缓存", roleId);
    }
}
