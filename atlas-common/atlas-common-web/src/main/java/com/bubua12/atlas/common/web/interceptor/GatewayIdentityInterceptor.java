package com.bubua12.atlas.common.web.interceptor;

import com.bubua12.atlas.common.core.exception.BusinessErrorCode;
import com.bubua12.atlas.common.core.exception.BusinessException;
import com.bubua12.atlas.common.core.model.GatewayUserContext;
import com.bubua12.atlas.common.security.service.RequestSignatureService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;

import static com.bubua12.atlas.common.core.constant.RequestHeaderConstants.*;

/**
 * 验证网关下发的带签名用户身份断言。
 *
 * <p>这个拦截器只做“验签”和“把可信结果塞进 request attribute”，
 * 不直接写 {@code SecurityContextHolder}，这样可以把“可信身份建立”与“线程上下文填充”拆成两步。
 */
@Slf4j
@RequiredArgsConstructor
public class GatewayIdentityInterceptor implements HandlerInterceptor {

    // 请求签名与验签服务
    private final RequestSignatureService requestSignatureService;

    /**
     * 下游服务最先被拦截进行逻辑处理，即验签
     *
     * @param request  current HTTP request
     * @param response current HTTP response
     * @param handler  chosen handler to execute, for type and/or instance evaluation
     */
    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        // 对于一些公共接口、健康检查、不依赖用户身份的请求，直接放行
        if (!hasGatewayIdentityAttempt(request)) {
            return true;
        }

        // 获取请求头进行签名计算
        String payload = request.getHeader(X_LOGIN_USER);
        String timestamp = request.getHeader(X_GATEWAY_TIMESTAMP);
        String signature = request.getHeader(X_GATEWAY_SIGNATURE);
        if (isMissing(payload, timestamp, signature)) {
            throw new BusinessException("缺少网关身份签名标头，请检查请求是否来自网关", BusinessErrorCode.UNAUTHORIZED);
        }

        // 验证网关签名
        String methodName = request.getMethod();
        String path = request.getRequestURI();
        log.debug("【网关请求签名拦截器验证】参数：method: {}，path: {}，payload: {}，timestamp: {}，signature: {}", methodName, path, payload, timestamp, signature);
        GatewayUserContext userContext = requestSignatureService.verifyGatewayRequest(methodName, path, payload, timestamp, signature);

        request.setAttribute(ATTR_GATEWAY_USER_CONTEXT, userContext);
        return true;
    }

    /**
     * 对于一些公共接口、健康检查、不依赖用户身份的请求，直接放行
     */
    private boolean hasGatewayIdentityAttempt(HttpServletRequest request) {
        // 兼容识别旧的 X-User-* 头和新的网关签名头，只要有人尝试声明“我是某个用户”，就进入强校验。
        return hasText(request.getHeader(AUTHORIZATION))
                || hasText(request.getHeader(X_USER_ID))
                || hasText(request.getHeader(X_USER_NAME))
                || hasText(request.getHeader(X_LOGIN_USER))
                || hasText(request.getHeader(X_GATEWAY_TIMESTAMP))
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
