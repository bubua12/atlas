package com.bubua12.atlas.gateway.filter;

import com.bubua12.atlas.common.core.model.LoginUser;
import com.bubua12.atlas.common.redis.service.RedisService;
import com.bubua12.atlas.common.security.utils.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

/**
 * 网关认证过滤器（优化版）
 * <p>
 * 优化点：
 * 1. 验证 Token 后，从 Redis 获取 LoginUser（一次查询）
 * 2. 将 LoginUser 序列化并通过请求头传递给下游服务
 * 3. 生成 HMAC 签名防止请求头伪造
 * 4. 下游服务无需再查询 Redis，直接使用请求头中的用户信息
 */
@Slf4j
@Component
public class AuthFilter implements GlobalFilter, Ordered {

    @Resource
    private JwtUtils jwtUtils;

    @Resource
    private RedisService redisService;

    @Resource
    private ObjectMapper objectMapper;

    @Value("${atlas.gateway.secret}")
    private String gatewaySecret;

    private static final String TOKEN_CACHE_PREFIX = "auth:token:";

    /**
     * 白名单 不需要认证
     */
    private static final List<String> WHITELIST = List.of(
            "/auth/login",
            "/auth/captcha",
            "/auth/register",
            "/auth/wecom/config"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.debug("execute filter... ...");
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 白名单跳过校验
        if (isWhitelisted(path)) {
            return chain.filter(exchange);
        }

        // Check for Authorization header
        String token = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (token == null || token.isBlank()) {
            log.info("请求头获取Token为空");
            return unauthorizedResponse(exchange.getResponse());
        }

        // 去除 Bearer 前缀
        String rawToken = token;
        if (token.startsWith("Bearer ")) {
            rawToken = token.substring(7);
        }

        // 验证 Token 并获取 LoginUser（一次 Redis 查询）
        if (jwtUtils.isTokenExpired(rawToken)) {
            log.info("JWT校验提示为空，{}", rawToken);
            return unauthorizedResponse(exchange.getResponse());
        }

        LoginUser loginUser = redisService.get(TOKEN_CACHE_PREFIX + rawToken);
        if (loginUser == null) {
            log.info("Redis里的LoginUser为空");
            return unauthorizedResponse(exchange.getResponse());
        }

        try {
            // 将 LoginUser 序列化为 JSON 并 Base64 编码
            String loginUserJson = objectMapper.writeValueAsString(loginUser);
            String encodedLoginUser = Base64.getEncoder().encodeToString(
                    loginUserJson.getBytes(StandardCharsets.UTF_8));

            // 生成签名防止伪造
            String timestamp = String.valueOf(System.currentTimeMillis());
            String signature = generateSignature(loginUser.getUserId().toString(), timestamp);

            // 传递用户信息和签名到下游服务
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("Authorization", rawToken)  // 传递纯 Token（无 Bearer 前缀）
                    .header("X-User-Id", loginUser.getUserId().toString())
                    .header("X-User-Name", loginUser.getUsername())
                    .header("X-Login-User", encodedLoginUser)  // 完整用户信息
                    .header("X-Gateway-Timestamp", timestamp)
                    .header("X-Gateway-Signature", signature)
                    .build();

            log.debug("网关验证通过: userId={}, username={}", loginUser.getUserId(), loginUser.getUsername());
            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        } catch (Exception e) {
            log.error("序列化 LoginUser 失败", e);
            return unauthorizedResponse(exchange.getResponse());
        }
    }

    @Override
    public int getOrder() {
        return -100;
    }

    /**
     * Check whether the request path is in the whitelist.
     */
    private boolean isWhitelisted(String path) {
        return WHITELIST.stream().anyMatch(path::startsWith);
    }

    /**
     * Write a 401 JSON response.
     */
    private Mono<Void> unauthorizedResponse(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"code\":401,\"msg\":\"Unauthorized, token is missing or invalid(Atlas Gateway)\"}";
        DataBuffer buffer = response.bufferFactory()
                .wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    /**
     * 生成 HMAC-SHA256 签名
     */
    private String generateSignature(String userId, String timestamp) {
        String data = userId + ":" + timestamp;
        return new HmacUtils(HmacAlgorithms.HMAC_SHA_256, gatewaySecret).hmacHex(data);
    }
}
