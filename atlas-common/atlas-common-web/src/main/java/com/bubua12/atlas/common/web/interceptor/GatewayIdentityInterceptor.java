package com.bubua12.atlas.common.web.interceptor;

import com.bubua12.atlas.common.core.exception.BusinessErrorCode;
import com.bubua12.atlas.common.core.exception.BusinessException;
import com.bubua12.atlas.common.core.model.GatewayUserContext;
import com.bubua12.atlas.common.security.service.RequestSignatureService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;

import static com.bubua12.atlas.common.core.constant.RequestHeaderConstants.*;

/**
 * 验证网关下发的带签名用户身份断言。
 *
 * <p>这个拦截器只做“验签”和“把可信结果塞进 request attribute”，
 * 不直接写 {@code SecurityContextHolder}，这样可以把“可信身份建立”与“线程上下文填充”拆成两步。
 */
@RequiredArgsConstructor
public class GatewayIdentityInterceptor implements HandlerInterceptor {

    private final RequestSignatureService requestSignatureService;

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler
    ) {
        if (!hasGatewayIdentityAttempt(request)) {
            return true;
        }

        // 只要出现任何身份相关头，就必须携带完整签名包，避免半套 Header 混进来继续污染上下文。
        String payload = request.getHeader(X_LOGIN_USER);
        String timestamp = request.getHeader(X_GATEWAY_TIMESTAMP);
        String nonce = request.getHeader(X_GATEWAY_NONCE);
        String signature = request.getHeader(X_GATEWAY_SIGNATURE);
        if (isMissing(payload, timestamp, nonce, signature)) {
            throw new BusinessException("Missing gateway identity signature headers", BusinessErrorCode.UNAUTHORIZED);
        }

        GatewayUserContext userContext = requestSignatureService.verifyGatewayRequest(
                request.getMethod(),
                request.getRequestURI(),
                payload,
                timestamp,
                nonce,
                signature
        );
        request.setAttribute(ATTR_GATEWAY_USER_CONTEXT, userContext);
        return true;
    }

    private boolean hasGatewayIdentityAttempt(HttpServletRequest request) {
        // 兼容识别旧的 X-User-* 头和新的网关签名头，只要有人尝试声明“我是某个用户”，就进入强校验。
        return hasText(request.getHeader(AUTHORIZATION))
                || hasText(request.getHeader(X_USER_ID))
                || hasText(request.getHeader(X_USER_NAME))
                || hasText(request.getHeader(X_LOGIN_USER))
                || hasText(request.getHeader(X_GATEWAY_TIMESTAMP))
                || hasText(request.getHeader(X_GATEWAY_NONCE))
                || hasText(request.getHeader(X_GATEWAY_SIGNATURE));
    }

    private boolean isMissing(String... values) {
        for (String value : values) {
            if (!hasText(value)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
