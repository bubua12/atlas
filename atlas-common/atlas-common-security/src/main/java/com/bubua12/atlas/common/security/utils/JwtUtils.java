package com.bubua12.atlas.common.security.utils;

import com.bubua12.atlas.common.core.model.LoginUser;
import com.bubua12.atlas.common.redis.service.RedisService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.bubua12.atlas.common.core.constant.RequestHeaderConstants.BEARER_PREFIX;

/**
 * JWT 工具类。
 */
@Component
public class JwtUtils {

    private static final String CLAIM_USER_ID = "user_id";
    private static final String CLAIM_USERNAME = "username";
    private static final String TOKEN_CACHE_PREFIX = "auth:token:";

    @Resource
    private RedisService redisService;

    @Value("${atlas.jwt.secret}")
    private String secret;

    @Value("${atlas.jwt.expiration}")
    private long expiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

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

    public Claims parseToken(String token) {
        String rawToken = normalizeToken(token);
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(rawToken)
                .getPayload();
    }

    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        return claims.get(CLAIM_USER_ID, Long.class);
    }

    public String getUsername(String token) {
        Claims claims = parseToken(token);
        return claims.get(CLAIM_USERNAME, String.class);
    }

    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public boolean isTokenValid(String token) {
        String rawToken = normalizeToken(token);
        if (!StringUtils.hasText(rawToken)) {
            return false;
        }
        if (isTokenExpired(rawToken)) {
            return false;
        }
        return Boolean.TRUE.equals(redisService.hasKey(TOKEN_CACHE_PREFIX + rawToken));
    }

    public String normalizeToken(String token) {
        if (!StringUtils.hasText(token)) {
            return null;
        }
        String rawToken = token.trim();
        if (rawToken.startsWith(BEARER_PREFIX)) {
            rawToken = rawToken.substring(BEARER_PREFIX.length()).trim();
        }
        return rawToken;
    }

    public LoginUser getLoginUser(String token) {
        String rawToken = normalizeToken(token);
        if (!StringUtils.hasText(rawToken)) {
            return null;
        }
        return redisService.get(TOKEN_CACHE_PREFIX + rawToken);
    }
}
