package com.bubua12.atlas.common.web.interceptor;

import cn.hutool.core.util.StrUtil;
import com.bubua12.atlas.common.core.constant.Constants;
import com.bubua12.atlas.common.core.constant.SecurityHeaderConstants;
import com.bubua12.atlas.common.core.exception.BusinessErrorCode;
import com.bubua12.atlas.common.core.exception.BusinessException;
import com.bubua12.atlas.common.core.utils.RequestSignatureUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 验证来自网关或内部服务的请求签名，避免业务服务直接信任透传请求头。
 */
public class RequestSecurityInterceptor implements HandlerInterceptor {

    private final String requestSignatureSecret;

    private final long allowedClockSkewMillis;

    public RequestSecurityInterceptor(String requestSignatureSecret, long allowedClockSkewMillis) {
        this.requestSignatureSecret = requestSignatureSecret;
        this.allowedClockSkewMillis = allowedClockSkewMillis;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String callerService = request.getHeader(SecurityHeaderConstants.CALLER_SERVICE);
        String timestamp = request.getHeader(SecurityHeaderConstants.REQUEST_TIMESTAMP);
        String signature = request.getHeader(SecurityHeaderConstants.REQUEST_SIGNATURE);
        String authorization = request.getHeader(Constants.TOKEN_HEADER);
        String userId = request.getHeader(SecurityHeaderConstants.USER_ID);
        String username = request.getHeader(SecurityHeaderConstants.USER_NAME);
        String encodedLoginUser = request.getHeader(SecurityHeaderConstants.LOGIN_USER);

        if (!hasSecurityHeaders(callerService, timestamp, signature, authorization, userId, username, encodedLoginUser)) {
            return true;
        }

        if (isPublicPath(request.getRequestURI())
                && StrUtil.isNotBlank(authorization)
                && StrUtil.isAllBlank(callerService, timestamp, signature, userId, username, encodedLoginUser)) {
            return true;
        }

        if (StrUtil.hasBlank(callerService, timestamp, signature)) {
            throw new BusinessException(BusinessErrorCode.UNAUTHORIZED);
        }

        validateTimestamp(timestamp);

        String expectedSignature = RequestSignatureUtils.sign(requestSignatureSecret, callerService, timestamp,
                authorization, userId, username, encodedLoginUser);
        if (!RequestSignatureUtils.matches(expectedSignature, signature)) {
            throw new BusinessException(BusinessErrorCode.UNAUTHORIZED);
        }

        if (hasUserIdentity(authorization, userId, username, encodedLoginUser)
                && !StrUtil.equals(callerService, SecurityHeaderConstants.GATEWAY_SERVICE)) {
            throw new BusinessException(BusinessErrorCode.FORBIDDEN);
        }

        request.setAttribute(SecurityHeaderConstants.ATTR_TRUSTED_REQUEST, Boolean.TRUE);
        request.setAttribute(SecurityHeaderConstants.ATTR_CALLER_SERVICE, callerService);
        return true;
    }

    private boolean hasSecurityHeaders(String callerService, String timestamp, String signature, String authorization,
                                       String userId, String username, String encodedLoginUser) {
        return StrUtil.isNotBlank(callerService)
                || StrUtil.isNotBlank(timestamp)
                || StrUtil.isNotBlank(signature)
                || StrUtil.isNotBlank(authorization)
                || StrUtil.isNotBlank(userId)
                || StrUtil.isNotBlank(username)
                || StrUtil.isNotBlank(encodedLoginUser);
    }

    private boolean hasUserIdentity(String authorization, String userId, String username, String encodedLoginUser) {
        return StrUtil.isNotBlank(authorization)
                || StrUtil.isNotBlank(userId)
                || StrUtil.isNotBlank(username)
                || StrUtil.isNotBlank(encodedLoginUser);
    }

    private void validateTimestamp(String timestamp) {
        long requestTimestamp;
        try {
            requestTimestamp = Long.parseLong(timestamp);
        } catch (NumberFormatException e) {
            throw new BusinessException(BusinessErrorCode.UNAUTHORIZED);
        }

        long timeDelta = Math.abs(System.currentTimeMillis() - requestTimestamp);
        if (timeDelta > allowedClockSkewMillis) {
            throw new BusinessException(BusinessErrorCode.UNAUTHORIZED);
        }
    }

    private boolean isPublicPath(String requestUri) {
        return StrUtil.startWithAny(requestUri, "/login", "/captcha", "/register", "/wecom/config", "/oauth2/");
    }
}
