package com.bubua12.atlas.gateway.filter;

import com.bubua12.atlas.common.security.utils.JwtUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
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
import java.util.List;

/**
 * Global authentication filter for gateway.
 * Checks Authorization header and forwards user info to downstream services.
 */
@Slf4j
@Component
public class AuthFilter implements GlobalFilter, Ordered {

    @Resource
    private JwtUtils jwtUtils;

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
            return unauthorizedResponse(exchange.getResponse());
        }

        if (!jwtUtils.isTokenValid(token)) {
            return unauthorizedResponse(exchange.getResponse());
        }

        ServerHttpRequest mutatedRequest = request.mutate()
                .header("X-User-Id", getUserId(token))
                .header("X-User-Name", getUserName(token))
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
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
        String body = "{\"code\":401,\"msg\":\"Unauthorized, token is missing or invalid\"}";
        DataBuffer buffer = response.bufferFactory()
                .wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    /**
     * Extract userId from token. Placeholder — replace with real JWT parsing.
     * 这里传下去的还是临时值，需要从token中解析出来用户信息
     */
    private String getUserId(String token) {
        String userId = jwtUtils.getUserId(token).toString();
        log.info("当前登录用户ID: {}", userId);
        return userId;
    }

    /**
     * Extract username from token. Placeholder — replace with real JWT parsing.
     */
    private String getUserName(String token) {
        String username = jwtUtils.getUsername(token);
        log.info("当前登录用户名称: {}", username);
        return username;
    }
}
