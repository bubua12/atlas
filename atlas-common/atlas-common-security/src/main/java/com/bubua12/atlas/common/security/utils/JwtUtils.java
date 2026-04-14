package com.bubua12.atlas.common.security.utils;

import com.bubua12.atlas.common.redis.service.RedisService;
import com.bubua12.atlas.common.core.utils.TokenUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 * 负责令牌的生成、解析、校验，基于 HMAC-SHA 签名算法。
 */
@Component
public class JwtUtils {

    @Resource
    private RedisService redisService;

    /**
     * JWT 签名密钥
     */
    @Value("${atlas.jwt.secret}")
    private String secret;

    /**
     * JWT 过期时间（秒）
     */
    @Value("${atlas.jwt.expiration}")
    private long expiration;

    private static final String CLAIM_USER_ID = "user_id";
    private static final String CLAIM_USERNAME = "username";
    private static final String TOKEN_CACHE_PREFIX = "auth:token:";

    /**
     * 获取 HMAC 签名密钥
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 JWT 令牌
     */
    public String generateToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_USER_ID, userId);
        claims.put(CLAIM_USERNAME, username);

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration * 1000))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 解析 JWT 令牌，返回 Claims
     */
    public Claims parseToken(String token) {
        String rawToken = TokenUtils.resolveToken(token);
        if (rawToken == null) {
            throw new IllegalArgumentException("Token is blank");
        }

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(rawToken)
                .getPayload();
    }

    /**
     * 从令牌中提取用户ID
     */
    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        return claims.get(CLAIM_USER_ID, Long.class);
    }

    /**
     * 从令牌中提取用户名
     */
    public String getUsername(String token) {
        Claims claims = parseToken(token);
        return claims.get(CLAIM_USERNAME, String.class);
    }

    /**
     * 判断令牌是否已过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 校验 token 是否有效（未过期且在 Redis 中存在）
     */
    public boolean isTokenValid(String token) {
        String rawToken = TokenUtils.resolveToken(token);
        if (rawToken == null || isTokenExpired(rawToken)) {
            return false;
        }
        return Boolean.TRUE.equals(redisService.hasKey(TOKEN_CACHE_PREFIX + rawToken));
    }
}
