package com.bubua12.atlas.auth.listener;

import com.bubua12.atlas.api.auth.constant.AuthCacheConstant;
import com.bubua12.atlas.common.core.model.LoginUser;
import com.bubua12.atlas.common.redis.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 订阅登录态刷新事件，静默更新 Redis 中已登录用户的权限与数据权限，无需重新登录。
 * 消息格式：user:{userId}:{dataScope}:{deptId}:{perm1,perm2,...}
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PermissionChangeListener implements MessageListener {

    private final RedisService redisService;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String body = new String(message.getBody(), StandardCharsets.UTF_8);
        if (!body.startsWith("user:")) {
            return;
        }

        // 事件负载是轻量字符串协议，避免 system/auth 为刷新登录态再引入额外 DTO 依赖。
        String[] parts = body.split(":", 5);
        if (parts.length < 4) {
            return;
        }

        long userId = Long.parseLong(parts[1]);
        Integer dataScope = "null".equals(parts[2]) ? null : Integer.parseInt(parts[2]);
        Long deptId = "null".equals(parts[3]) ? null : Long.parseLong(parts[3]);
        Set<String> newPerms = parts.length == 5 && !parts[4].isBlank()
                ? Arrays.stream(parts[4].split(",")).collect(Collectors.toSet())
                : Set.of();

        // 目前 token -> LoginUser 是唯一索引，所以先扫描登录态再按 userId 过滤；后续可演进成 userId -> tokens 反向索引。
        Set<String> keys = redisService.keys(AuthCacheConstant.AUTH_TOKEN_CACHE_PREFIX + "*");
        if (keys == null) {
            return;
        }

        for (String key : keys) {
            LoginUser user = redisService.get(key);
            if (user != null && Long.valueOf(userId).equals(user.getUserId())) {
                user.setPermissions(newPerms);
                user.setDataScope(dataScope);
                user.setDeptId(deptId);
                Long ttl = redisService.getExpire(key);
                redisService.set(key, user, ttl != null && ttl > 0 ? ttl : 7200, TimeUnit.SECONDS);
                log.info("登录态刷新，静默更新用户 {} 的权限/数据权限缓存", userId);
            }
        }
    }
}
