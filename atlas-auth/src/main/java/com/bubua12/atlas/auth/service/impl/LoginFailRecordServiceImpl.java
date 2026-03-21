package com.bubua12.atlas.auth.service.impl;

import com.bubua12.atlas.api.auth.constant.AuthCacheConstant;
import com.bubua12.atlas.auth.service.LoginFailRecordService;
import com.bubua12.atlas.common.redis.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 登录失败记录服务实现
 * 纯 Redis 实现：INCR 原子计数 + TTL 自动过期锁定
 */
@Service
@RequiredArgsConstructor
public class LoginFailRecordServiceImpl implements LoginFailRecordService {

    private final RedisService redisService;

    // TODO: 从系统配置读取
    private static final int IP_FAIL_MAX = 5;
    private static final int ACCOUNT_FAIL_MAX = 5;
    private static final int LOCK_DURATION_MINUTES = 30;


    @Override
    public void recordLoginFail(String ip, String username) {
        recordFailToRedis(AuthCacheConstant.AUTH_FAIL_IP_PREFIX + ip,
                AuthCacheConstant.AUTH_LOCK_IP_PREFIX + ip, IP_FAIL_MAX);
        recordFailToRedis(AuthCacheConstant.AUTH_FAIL_ACCOUNT_PREFIX + username,
                AuthCacheConstant.AUTH_LOCK_ACCOUNT_PREFIX + username, ACCOUNT_FAIL_MAX);
    }

    @Override
    public void clearLoginFail(String ip, String username) {
        redisService.delete(AuthCacheConstant.AUTH_FAIL_IP_PREFIX + ip);
        redisService.delete(AuthCacheConstant.AUTH_LOCK_IP_PREFIX + ip);
        redisService.delete(AuthCacheConstant.AUTH_FAIL_ACCOUNT_PREFIX + username);
        redisService.delete(AuthCacheConstant.AUTH_LOCK_ACCOUNT_PREFIX + username);
    }

    /**
     * Redis INCR 原子计数，首次设 TTL，达阈值写入锁定 key
     */
    private void recordFailToRedis(String failKey, String lockKey, int maxFail) {
        Long count = redisService.increment(failKey);
        if (count != null && count == 1) {
            redisService.expire(failKey, LOCK_DURATION_MINUTES, TimeUnit.MINUTES);
        }
        if (count != null && count >= maxFail) {
            redisService.set(lockKey, 1, LOCK_DURATION_MINUTES, TimeUnit.MINUTES);
        }
    }
}
