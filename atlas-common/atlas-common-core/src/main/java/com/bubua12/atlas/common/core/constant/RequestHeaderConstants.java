package com.bubua12.atlas.common.core.constant;

/**
 * 统一的安全头协议。
 *
 * <p>这里同时约束两条链路：
 * 1. 网关向下游服务转发“已验证用户身份”时使用的请求头；
 * 2. 内部 Feign 调用证明“调用方服务是谁”时使用的请求头。
 * 这样可以避免各模块散落硬编码字符串，后续协议调整时只改一个地方。
 *
 * @author bubua12
 * @since 2026/4/15 16:47
 */
public final class RequestHeaderConstants {
    private RequestHeaderConstants() {
    }

    public static final String BEARER_PREFIX = "Bearer ";

    public static final String X_USER_ID = "X-User-Id";
    public static final String X_USER_NAME = "X-User-Name";
    public static final String AUTHORIZATION = "Authorization";

    /**
     * 网关签发给下游服务的已验证用户断言。
     */
    public static final String X_LOGIN_USER = "X-Login-User";
    public static final String X_GATEWAY_TIMESTAMP = "X-Gateway-Timestamp";
    public static final String X_GATEWAY_NONCE = "X-Gateway-Nonce";
    public static final String X_GATEWAY_SIGNATURE = "X-Gateway-Signature";

    /**
     * 内部服务调用认证头，只用于 Feign / 内部 HTTP 调用，不承载用户身份。
     */
    public static final String X_INTERNAL_SERVICE = "X-Internal-Service";
    public static final String X_INTERNAL_TIMESTAMP = "X-Internal-Timestamp";
    public static final String X_INTERNAL_NONCE = "X-Internal-Nonce";
    public static final String X_INTERNAL_SIGNATURE = "X-Internal-Signature";

    /**
     * 经过验签后的上下文只在服务端 request attribute 内流转，不再回写到原始 Header。
     */
    public static final String ATTR_GATEWAY_USER_CONTEXT =
            "com.bubua12.atlas.gateway.user-context";
    public static final String ATTR_INTERNAL_CALLER_SERVICE =
            "com.bubua12.atlas.internal.caller-service";
}
