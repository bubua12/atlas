package com.bubua12.atlas.monitor.service;

import com.bubua12.atlas.common.core.model.LoginUser;
import com.bubua12.atlas.common.redis.service.RedisService;
import com.bubua12.atlas.common.security.utils.JwtUtils;
import com.bubua12.atlas.monitor.vo.OnlineUserVO;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 在线用户服务
 */
@Service
public class OnlineUserService {

    @Resource
    private RedisService redisService;

    @Resource
    private JwtUtils jwtUtils;

    private static final String TOKEN_CACHE_PREFIX = "auth:token:";

    /**
     * 获取在线用户列表
     */
    public List<OnlineUserVO> listOnlineUsers() {
        List<OnlineUserVO> result = new ArrayList<>();
        Set<String> keys = redisService.keys(TOKEN_CACHE_PREFIX + "*");

        if (keys == null || keys.isEmpty()) {
            return result;
        }

        for (String key : keys) {
            LoginUser loginUser = redisService.get(key);
            if (loginUser == null) {
                continue;
            }

            OnlineUserVO vo = new OnlineUserVO();
            vo.setUserId(loginUser.getUserId());
            vo.setUsername(loginUser.getUsername());
            vo.setToken(loginUser.getToken());
            vo.setClientIp(loginUser.getClientIp());

            try {
                Claims claims = jwtUtils.parseToken(loginUser.getToken());
                vo.setLoginTime(LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(claims.getIssuedAt().getTime()),
                        ZoneId.systemDefault()
                ));
                vo.setExpireTime(LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(claims.getExpiration().getTime()),
                        ZoneId.systemDefault()
                ));
            } catch (Exception ignored) {
            }

            result.add(vo);
        }

        return result;
    }

    /**
     * 强制用户下线
     */
    public void kickOut(String token) {
        redisService.delete(TOKEN_CACHE_PREFIX + token);
    }
}
