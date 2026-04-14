package com.bubua12.atlas.common.web.interceptor;

import com.bubua12.atlas.common.core.context.SecurityContextHolder;
import com.bubua12.atlas.common.core.constant.Constants;
import com.bubua12.atlas.common.core.constant.SecurityHeaderConstants;
import com.bubua12.atlas.common.core.model.LoginUser;
import com.bubua12.atlas.common.core.utils.TokenUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 用户上下文拦截器
 * 从网关透传并验签后的请求头中恢复用户上下文。
 */
@RequiredArgsConstructor
public class UserContextInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        SecurityContextHolder.clear();

        String authorization = request.getHeader(Constants.TOKEN_HEADER);
        if (authorization != null) {
            SecurityContextHolder.setToken(authorization);
        }

        String encodedLoginUser = request.getHeader(SecurityHeaderConstants.LOGIN_USER);
        if (encodedLoginUser != null) {
            LoginUser loginUser = deserializeLoginUser(encodedLoginUser);
            SecurityContextHolder.setLoginUser(loginUser);
            SecurityContextHolder.setUserId(loginUser.getUserId());
            SecurityContextHolder.setUsername(loginUser.getUsername());
            if (authorization == null) {
                SecurityContextHolder.setToken(TokenUtils.withBearerPrefix(loginUser.getToken()));
            }
            return true;
        }

        String userId = request.getHeader(SecurityHeaderConstants.USER_ID);
        try {
            if (userId != null) {
                SecurityContextHolder.setUserId(Long.valueOf(userId));
            }
            String username = request.getHeader(SecurityHeaderConstants.USER_NAME);
            if (username != null) {
                SecurityContextHolder.setUsername(username);
            }
        } catch (NumberFormatException e) {
            SecurityContextHolder.clear();
            throw new com.bubua12.atlas.common.core.exception.BusinessException(
                    com.bubua12.atlas.common.core.exception.BusinessErrorCode.UNAUTHORIZED);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 请求处理完成后清理 ThreadLocal，防止内存泄漏
        SecurityContextHolder.clear();
    }

    private LoginUser deserializeLoginUser(String encodedLoginUser) {
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(encodedLoginUser);
            return objectMapper.readValue(new String(decoded, StandardCharsets.UTF_8), LoginUser.class);
        } catch (IllegalArgumentException | IOException e) {
            SecurityContextHolder.clear();
            throw new com.bubua12.atlas.common.core.exception.BusinessException(
                    com.bubua12.atlas.common.core.exception.BusinessErrorCode.UNAUTHORIZED);
        }
    }
}
