package com.bubua12.atlas.common.web.interceptor;

import com.bubua12.atlas.common.core.exception.BusinessErrorCode;
import com.bubua12.atlas.common.core.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 网关签名验证拦截器
 * 验证请求是否来自合法网关，防止请求伪造
 * 工作原理：
 * 1. 网关使用 HMAC-SHA256 对 userId + timestamp 进行签名
 * 2. 下游服务验证签名是否匹配
 * 3. 验证时间戳是否在 5 分钟内（防重放攻击）
 */
@Slf4j
public class GatewaySignatureInterceptor implements HandlerInterceptor {

    @Value("${atlas.gateway.secret}")
    private String gatewaySecret;

    private static final long MAX_TIME_DIFF = 300000; // 5 分钟

    // @NonNull注解的用法
    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response,
                             @NonNull Object handler) {
        String userId = request.getHeader("X-User-Id");
        String timestamp = request.getHeader("X-Gateway-Timestamp");
        String signature = request.getHeader("X-Gateway-Signature");

        // 公开接口跳过验证（无用户信息的请求）
        if (userId == null || timestamp == null || signature == null) {
            log.debug("跳过签名验证：公开接口");
            return true;
        }

        // 验证时间戳（防重放攻击，5 分钟内有效）
        try {
            long requestTime = Long.parseLong(timestamp);
            long currentTime = System.currentTimeMillis();
            if (currentTime - requestTime > MAX_TIME_DIFF) {
                log.warn("请求已过期: requestTime={}, currentTime={}, diff={}ms",
                        requestTime, currentTime, currentTime - requestTime);
                throw new BusinessException(BusinessErrorCode.UNAUTHORIZED);
            }
        } catch (NumberFormatException e) {
            log.warn("时间戳格式错误: {}", timestamp);
            throw new BusinessException(BusinessErrorCode.UNAUTHORIZED);
        }

        // 验证签名
        String expectedSignature = new HmacUtils(HmacAlgorithms.HMAC_SHA_256, gatewaySecret).hmacHex(userId + ":" + timestamp);
        if (!expectedSignature.equals(signature)) {
            log.warn("签名验证失败 - userId: {}, timestamp: {}, expected: {}, actual: {}",
                    userId, timestamp, expectedSignature, signature);
            throw new BusinessException(BusinessErrorCode.UNAUTHORIZED);
        }

        log.debug("签名验证通过: userId={}", userId);
        return true;
    }
}
