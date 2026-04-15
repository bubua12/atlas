package com.bubua12.atlas.gateway.filter;

import com.bubua12.atlas.common.core.model.GatewayUserContext;
import com.bubua12.atlas.common.core.model.LoginUser;
import com.bubua12.atlas.common.security.service.RequestSignatureService;
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

import static com.bubua12.atlas.common.core.constant.RequestHeaderConstants.*;

/**
 * 网关统一认证过滤器。
 *
 * <p>职责有两层：
 * 1. 认证外部请求携带的 token；
 * 2. 把认证结果重新签成“下游服务可验证的用户断言”再转发。
 * 下游从这一刻开始只信签名断言，不再信任客户端原始 Header。
 */
@Slf4j
@Component
public class AuthFilter implements GlobalFilter, Ordered {

    private static final List<String> WHITELIST = List.of(
            "/auth/login",
            "/auth/captcha",
            "/auth/register",
            "/auth/wecom/config"
    );

    @Resource
    private JwtUtils jwtUtils;

    @Resource
    private RequestSignatureService requestSignatureService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (isWhitelisted(path)) {
            // 白名单接口不做登录校验，但仍然要清洗掉外部伪造的安全头，避免脏数据流到下游。
            return chain.filter(exchange.mutate()
                    .request(sanitizeSecurityHeaders(request))
                    .build());
        }

        String token = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (token == null || token.isBlank()) {
            return unauthorizedResponse(exchange.getResponse());
        }

        if (!jwtUtils.isTokenValid(token)) {
            return unauthorizedResponse(exchange.getResponse());
        }

        LoginUser loginUser = jwtUtils.getLoginUser(token);
        if (loginUser == null) {
            return unauthorizedResponse(exchange.getResponse());
        }

        // gateway 签名的 path 必须与下游最终收到的 path 一致，否则下游验签一定失败。
        String forwardedPath = toForwardedPath(path);
        String payload = requestSignatureService.encodeGatewayUserContext(
                GatewayUserContext.fromLoginUser(loginUser)
        );
        String timestamp = requestSignatureService.currentTimestamp();
        String nonce = requestSignatureService.newNonce();
        String signature = requestSignatureService.signGatewayRequest(
                request.getMethod().name(),
                forwardedPath,
                payload,
                timestamp,
                nonce
        );

        ServerHttpRequest mutatedRequest = request.mutate()
                .headers(headers -> {
                    // 无论客户端或上游是否带了这些头，最终都只允许网关自己签发的新值进入下游服务。
                    removeSecurityHeaders(headers);
                    headers.set(X_LOGIN_USER, payload);
                    headers.set(X_GATEWAY_TIMESTAMP, timestamp);
                    headers.set(X_GATEWAY_NONCE, nonce);
                    headers.set(X_GATEWAY_SIGNATURE, signature);
                })
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private boolean isWhitelisted(String path) {
        return WHITELIST.stream().anyMatch(path::startsWith);
    }

    private ServerHttpRequest sanitizeSecurityHeaders(ServerHttpRequest request) {
        return request.mutate()
                .headers(this::removeSecurityHeaders)
                .build();
    }

    private void removeSecurityHeaders(HttpHeaders headers) {
        headers.remove(X_USER_ID);
        headers.remove(X_USER_NAME);
        headers.remove(X_LOGIN_USER);
        headers.remove(X_GATEWAY_TIMESTAMP);
        headers.remove(X_GATEWAY_NONCE);
        headers.remove(X_GATEWAY_SIGNATURE);
        headers.remove(X_INTERNAL_SERVICE);
        headers.remove(X_INTERNAL_TIMESTAMP);
        headers.remove(X_INTERNAL_NONCE);
        headers.remove(X_INTERNAL_SIGNATURE);
    }

    /**
     * 当前网关路由统一使用 `StripPrefix=1`，签名时需要按下游服务实际接收的路径计算。
     *
     * <p>如果后面路由规则改了，这里也要跟着一起调整，否则会出现“请求能到下游但验签失败”的现象。
     */
    private String toForwardedPath(String originalPath) {
        if (originalPath == null || originalPath.isBlank() || "/".equals(originalPath)) {
            return "/";
        }
        int secondSlash = originalPath.indexOf('/', 1);
        return secondSlash >= 0 ? originalPath.substring(secondSlash) : "/";
    }

    private Mono<Void> unauthorizedResponse(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"code\":401,\"msg\":\"Unauthorized, token is missing or invalid\"}";
        DataBuffer buffer = response.bufferFactory()
                .wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}
