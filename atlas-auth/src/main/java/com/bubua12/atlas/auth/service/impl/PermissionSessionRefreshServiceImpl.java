package com.bubua12.atlas.auth.service.impl;

import com.bubua12.atlas.api.auth.constant.AuthCacheConstant;
import com.bubua12.atlas.api.system.dto.UserDTO;
import com.bubua12.atlas.auth.converter.AuthConverter;
import com.bubua12.atlas.auth.feign.AtlasSystemFeign;
import com.bubua12.atlas.auth.service.PermissionSessionRefreshService;
import com.bubua12.atlas.common.core.model.LoginUser;
import com.bubua12.atlas.common.core.result.CommonResult;
import com.bubua12.atlas.common.core.result.ResultCodeEnum;
import com.bubua12.atlas.common.redis.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 权限变更后刷新在线会话中的用户权限快照。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionSessionRefreshServiceImpl implements PermissionSessionRefreshService {

    private final RedisService redisService;

    private final AtlasSystemFeign atlasSystemFeign;

    private final AuthConverter authConverter;

    @Override
    public void refreshUserSessions(Set<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }

        for (Long userId : userIds) {
            if (userId == null) {
                continue;
            }
            refreshSingleUserSessions(userId);
        }
    }

    private void refreshSingleUserSessions(Long userId) {
        String userTokenIndexKey = AuthCacheConstant.AUTH_USER_TOKEN_SET_PREFIX + userId;
        Set<String> tokens = redisService.members(userTokenIndexKey);
        if (tokens == null || tokens.isEmpty()) {
            redisService.delete(userTokenIndexKey);
            return;
        }

        Set<String> staleTokens = new LinkedHashSet<>();
        Set<String> activeTokens = new LinkedHashSet<>();
        LoginUser sampleSession = null;
        long maxTtl = 0L;

        for (String token : tokens) {
            LoginUser loginUser = redisService.get(AuthCacheConstant.AUTH_TOKEN_CACHE_PREFIX + token);
            if (loginUser == null) {
                staleTokens.add(token);
                continue;
            }
            activeTokens.add(token);
            if (sampleSession == null) {
                sampleSession = loginUser;
            }
            Long ttl = redisService.getExpire(AuthCacheConstant.AUTH_TOKEN_CACHE_PREFIX + token, TimeUnit.SECONDS);
            if (ttl != null && ttl > maxTtl) {
                maxTtl = ttl;
            }
        }

        removeStaleTokens(userTokenIndexKey, staleTokens);
        if (sampleSession == null || activeTokens.isEmpty()) {
            redisService.delete(userTokenIndexKey);
            return;
        }

        CommonResult<UserDTO> result = atlasSystemFeign.getInternalUserById(userId);
        if (result == null || result.getCode() != ResultCodeEnum.RC200.getCode() || result.getData() == null) {
            log.warn("刷新用户在线会话失败，无法获取最新用户信息，userId={}, username={}", userId, sampleSession.getUsername());
            return;
        }

        UserDTO latestUser = result.getData();
        for (String token : activeTokens) {
            String tokenCacheKey = AuthCacheConstant.AUTH_TOKEN_CACHE_PREFIX + token;
            LoginUser currentSession = redisService.get(tokenCacheKey);
            if (currentSession == null) {
                redisService.removeFromSet(userTokenIndexKey, token);
                continue;
            }

            Long ttl = redisService.getExpire(tokenCacheKey, TimeUnit.SECONDS);
            if (ttl == null || ttl <= 0) {
                redisService.delete(tokenCacheKey);
                redisService.removeFromSet(userTokenIndexKey, token);
                continue;
            }

            LoginUser refreshedSession = authConverter.po2vo(latestUser);
            refreshedSession.setToken(token);
            refreshedSession.setClientIp(currentSession.getClientIp());
            redisService.set(tokenCacheKey, refreshedSession, ttl, TimeUnit.SECONDS);
            if (ttl > maxTtl) {
                maxTtl = ttl;
            }
        }

        if (maxTtl > 0) {
            redisService.expire(userTokenIndexKey, maxTtl, TimeUnit.SECONDS);
        } else {
            redisService.delete(userTokenIndexKey);
        }

        log.info("刷新用户在线会话完成，userId={}, tokenCount={}", userId, activeTokens.size());
    }

    private void removeStaleTokens(String userTokenIndexKey, Set<String> staleTokens) {
        if (staleTokens == null || staleTokens.isEmpty()) {
            return;
        }
        redisService.removeFromSet(userTokenIndexKey, staleTokens.toArray());
    }
}
