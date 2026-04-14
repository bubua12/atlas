package com.bubua12.atlas.common.core.constant;

/**
 * 安全链路透传请求头常量。
 */
public final class SecurityHeaderConstants {

    private SecurityHeaderConstants() {
    }

    public static final String USER_ID = "X-User-Id";

    public static final String USER_NAME = "X-User-Name";

    public static final String LOGIN_USER = "X-Login-User";

    public static final String CALLER_SERVICE = "X-Atlas-Caller-Service";

    public static final String REQUEST_TIMESTAMP = "X-Atlas-Timestamp";

    public static final String REQUEST_SIGNATURE = "X-Atlas-Signature";

    public static final String ATTR_TRUSTED_REQUEST = "atlas.trustedRequest";

    public static final String ATTR_CALLER_SERVICE = "atlas.callerService";

    public static final String GATEWAY_SERVICE = "atlas-gateway";
}
