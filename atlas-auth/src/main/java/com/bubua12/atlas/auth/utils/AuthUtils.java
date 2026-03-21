package com.bubua12.atlas.auth.utils;

import com.bubua12.atlas.api.auth.constant.AuthCacheConstant;
import com.bubua12.atlas.auth.exception.AuthErrorCode;
import com.bubua12.atlas.auth.exception.AuthException;
import com.bubua12.atlas.common.redis.utils.AtlasRedisUtils;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 登录工具类
 *
 * @author bubua12
 * @since 2026/03/21 22:15
 */
@Component
public class AuthUtils {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 通过 Redis Pipeline 一次网络往返检查 IP 和账号是否被锁定
     *
     * @param ip       客户端 IP
     * @param username 用户名
     */
    public void checkLoginLock(String ip, String username) {
        byte[] ipLockKey = AtlasRedisUtils.serialize(
                redisTemplate.getKeySerializer(),
                AuthCacheConstant.AUTH_LOCK_IP_PREFIX + ip);
        byte[] accountLockKey = AtlasRedisUtils.serialize(
                redisTemplate.getKeySerializer(),
                AuthCacheConstant.AUTH_LOCK_ACCOUNT_PREFIX + username);

        List<Object> results = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            connection.keyCommands().exists(ipLockKey);
            connection.keyCommands().exists(accountLockKey);
            return null;
        }, redisTemplate.getValueSerializer());

        Boolean ipLocked = (Boolean) results.get(0);
        Boolean accountLocked = (Boolean) results.get(1);

        if (Boolean.TRUE.equals(ipLocked)) {
            throw new AuthException(AuthErrorCode.IP_LOCKED);
        }
        if (Boolean.TRUE.equals(accountLocked)) {
            throw new AuthException(AuthErrorCode.ACCOUNT_LOCKED);
        }
    }
}
