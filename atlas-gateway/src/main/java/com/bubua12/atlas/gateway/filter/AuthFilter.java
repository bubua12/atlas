package com.bubua12.atlas.gateway.filter;

import com.bubua12.atlas.common.core.constant.Constants;
import com.bubua12.atlas.common.core.constant.SecurityHeaderConstants;
import com.bubua12.atlas.common.core.model.LoginUser;
import com.bubua12.atlas.common.core.utils.RequestSignatureUtils;
import com.bubua12.atlas.common.core.utils.TokenUtils;
import com.bubua12.atlas.common.redis.service.RedisService;
import com.bubua12.atlas.common.security.utils.JwtUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
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
 * Global authentication filter for gateway.
 * Checks Authorization header and forwards user info to downstream services.
 */
@Slf4j
@Component
public class AuthFilter implements GlobalFilter, Ordered {

    private static final String TOKEN_CACHE_PREFIX = "auth:token:";

    @Resource
    private JwtUtils jwtUtils;

    @Resource
    private RedisService redisService;

    @Resource
    private ObjectMapper objectMapper;

    @Value("${atlas.security.request-signature.secret:${ATLAS_REQUEST_SIGNATURE_SECRET:atlas-request-signature-secret-change-me}}")
    private String requestSignatureSecret;

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

        String rawToken = TokenUtils.resolveToken(token);
        if (rawToken == null || jwtUtils.isTokenExpired(rawToken)) {
            return unauthorizedResponse(exchange.getResponse());
        }

        LoginUser loginUser = redisService.get(TOKEN_CACHE_PREFIX + rawToken);
        if (loginUser == null) {
            return unauthorizedResponse(exchange.getResponse());
        }

        String encodedLoginUser = encodeLoginUser(loginUser);
        String timestamp = String.valueOf(System.currentTimeMillis());
        String signature = RequestSignatureUtils.sign(requestSignatureSecret, SecurityHeaderConstants.GATEWAY_SERVICE,
                timestamp, token, String.valueOf(loginUser.getUserId()), loginUser.getUsername(), encodedLoginUser);

        ServerHttpRequest mutatedRequest = request.mutate()
                .header(SecurityHeaderConstants.USER_ID, String.valueOf(loginUser.getUserId()))
                .header(SecurityHeaderConstants.USER_NAME, loginUser.getUsername())
                .header(SecurityHeaderConstants.LOGIN_USER, encodedLoginUser)
                .header(SecurityHeaderConstants.CALLER_SERVICE, SecurityHeaderConstants.GATEWAY_SERVICE)
                .header(SecurityHeaderConstants.REQUEST_TIMESTAMP, timestamp)
                .header(SecurityHeaderConstants.REQUEST_SIGNATURE, signature)
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

    private String encodeLoginUser(LoginUser loginUser) {
        try {
            String loginUserJson = objectMapper.writeValueAsString(loginUser);
            return Base64.getUrlEncoder().encodeToString(loginUserJson.getBytes(StandardCharsets.UTF_8));
        } catch (JsonProcessingException e) {
            log.error("序列化登录用户失败", e);
            throw new IllegalStateException("序列化登录用户失败", e);
        }
    }
}
