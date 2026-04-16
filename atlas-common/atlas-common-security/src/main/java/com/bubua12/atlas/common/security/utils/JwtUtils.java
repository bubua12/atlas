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
 *
 * <p>这里负责两件事：
 * 1. 生成和解析 JWT，自证 token 是否被当前密钥签发、是否过期；
 * 2. 配合 Redis 查询当前 token 对应的登录态是否仍然有效。
 *
 * <p>需要特别注意的是：
 * “JWT 能正常解析”只说明 token 结构合法、签名正确，
 * 不代表这个登录态没有被踢下线、注销或过期失效。
 * 真正的登录态有效性仍然以 Redis 中的 `auth:token:*` 缓存为准。
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

    /**
     * 整个服务统一使用同一把签名密钥，保证生成和解析使用的是同一套规则。
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 JWT 时只放最小身份字段。
     *
     * <p>权限、数据权限等可变信息仍然放在 Redis 的 `LoginUser` 中，
     * 这样后续更新角色或踢人下线时，不需要重新签发所有 token。
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
     * 只负责 JWT 本身的签名校验和 claims 解析。
     *
     * <p>这里不会检查 Redis 登录态是否还存在，所以 parse 成功不等于当前请求一定有权限继续访问。
     */
    public Claims parseToken(String token) {
        String rawToken = normalizeToken(token);
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(rawToken)
                .getPayload();
    }

    /**
     * 从 JWT claims 中解析用户 ID，适合需要轻量读取基础身份时使用。
     */
    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        return claims.get(CLAIM_USER_ID, Long.class);
    }

    /**
     * 从 JWT claims 中解析用户名，适合日志或最小身份恢复场景。
     */
    public String getUsername(String token) {
        Claims claims = parseToken(token);
        return claims.get(CLAIM_USERNAME, String.class);
    }

    /**
     * 过期判断只看 JWT 自带的 expiration 字段。
     *
     * <p>如果 token 解析失败，也统一按“已过期/无效”处理，便于上层直接拒绝请求。
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
     * 登录态有效性校验分三步：<br/>
     * 1. 归一化 Bearer token<br/>
     * 2. 校验 JWT 签名和过期时间<br/>
     * 3. 校验 Redis 中是否还存在对应登录态缓存。<br/>
     *
     * <p>只有三步都通过，才认为这个 token 当前可用。
     */
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

    /**
     * 统一兼容 `Bearer xxx` 和纯 token 两种格式，避免调用方各自手动截断前缀。
     */
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

    /**
     * 从 Redis 取完整登录态对象。
     *
     * <p>权限列表、部门、数据权限等都在这里恢复，供网关签名和权限切面复用。
     */
    public LoginUser getLoginUser(String token) {
        String rawToken = normalizeToken(token);
        if (!StringUtils.hasText(rawToken)) {
            return null;
        }
        return redisService.get(TOKEN_CACHE_PREFIX + rawToken);
    }
}
