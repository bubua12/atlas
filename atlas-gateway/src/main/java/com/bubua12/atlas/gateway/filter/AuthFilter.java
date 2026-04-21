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

        // 1、白名单接口清单放行
        if (isWhitelisted(path)) {
            // 白名单接口不做登录校验，但仍然要清洗掉外部伪造的安全头，避免脏数据流到下游。
            return chain.filter(exchange.mutate()
                    .request(sanitizeSecurityHeaders(request))
                    .build());
        }

        // 2、token校验，失败则返回未认证
        String token = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (token == null || token.isBlank()) {
            return unauthorizedResponse(exchange.getResponse());
        }

        if (!jwtUtils.isTokenValid(token)) {
            return unauthorizedResponse(exchange.getResponse());
        }

        // 3、读取 LoginUser
        LoginUser loginUser = jwtUtils.getLoginUser(token);
        if (loginUser == null) {
            return unauthorizedResponse(exchange.getResponse());
        }

        // 4、计算签名
        String methodName = request.getMethod().name();
        String forwardedPath = toForwardedPath(path);
        String payload = requestSignatureService.encodeGatewayUserContext(GatewayUserContext.fromLoginUser(loginUser));
        String timestamp = requestSignatureService.currentTimestamp();

        // 网关入口进行网关请求签名
        String signature = requestSignatureService.signGatewayRequest(methodName, forwardedPath, payload, timestamp);

        log.debug("[atlas-gateway] 网关计算签名: {}", signature);

        // 携带请求头进行下游
        ServerHttpRequest mutatedRequest = request.mutate()
                .headers(headers -> {
                    // 无论客户端或上游是否带了这些头，最终都只允许网关自己签发的新值进入下游服务。
                    removeSecurityHeaders(headers);
                    headers.set(X_LOGIN_USER, payload);
                    headers.set(X_GATEWAY_TIMESTAMP, timestamp);
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

    /**
     * 请求头清洗
     */
    private ServerHttpRequest sanitizeSecurityHeaders(ServerHttpRequest request) {
        return request.mutate()
                .headers(this::removeSecurityHeaders)
                .build();
    }

    /**
     * 请求头清洗：会先移除外部传进来的这些安全头，再自己重建，所以前端就算乱传，也不等于下游真正收到。
     * 不管客户端自己带了什么安全头，统统作废，只认网关重新签发的。
     */
    private void removeSecurityHeaders(HttpHeaders headers) {
        headers.remove(X_USER_ID);
        headers.remove(X_USER_NAME);
        headers.remove(X_LOGIN_USER);
        headers.remove(X_GATEWAY_TIMESTAMP);
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
