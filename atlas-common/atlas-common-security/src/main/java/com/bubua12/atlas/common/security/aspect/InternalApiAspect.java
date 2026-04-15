package com.bubua12.atlas.common.security.aspect;

import com.bubua12.atlas.common.core.constant.RequestHeaderConstants;
import com.bubua12.atlas.common.core.exception.BusinessErrorCode;
import com.bubua12.atlas.common.core.exception.BusinessException;
import com.bubua12.atlas.common.security.annotation.InternalApi;
import com.bubua12.atlas.common.security.service.RequestSignatureService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

import static com.bubua12.atlas.common.core.constant.RequestHeaderConstants.*;

/**
 * 内部服务接口认证切面。
 *
 * <p>它只负责保护标注了 {@link InternalApi} 的接口，
 * 证明“当前请求来自受信任的内部服务”，不替代用户权限校验。
 */
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@RequiredArgsConstructor
public class InternalApiAspect {

    private final RequestSignatureService requestSignatureService;

    @Around("@annotation(internalApi)")
    public Object around(ProceedingJoinPoint point, InternalApi internalApi) throws Throwable {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new BusinessException("Internal API requires an HTTP request context", BusinessErrorCode.FORBIDDEN);
        }

        HttpServletRequest request = attributes.getRequest();
        // 这里故意要求整套服务认证头齐全，避免仅靠一个服务名明文头就冒充内部调用。
        String callerService = request.getHeader(X_INTERNAL_SERVICE);
        String timestamp = request.getHeader(X_INTERNAL_TIMESTAMP);
        String nonce = request.getHeader(X_INTERNAL_NONCE);
        String signature = request.getHeader(X_INTERNAL_SIGNATURE);
        if (isMissing(callerService, timestamp, nonce, signature)) {
            throw new BusinessException("Missing internal service auth headers", BusinessErrorCode.FORBIDDEN);
        }

        String verifiedCaller = requestSignatureService.verifyInternalRequest(
                request.getMethod(),
                request.getRequestURI(),
                callerService,
                timestamp,
                nonce,
                signature
        );

        String[] allowedServices = internalApi.allowedServices();
        if (allowedServices.length > 0 && Arrays.stream(allowedServices).noneMatch(verifiedCaller::equals)) {
            throw new BusinessException("Internal service is not allowed: " + verifiedCaller, BusinessErrorCode.FORBIDDEN);
        }

        // 验证通过后的调用方身份放进 request，方便后续日志或调试场景复用。
        request.setAttribute(RequestHeaderConstants.ATTR_INTERNAL_CALLER_SERVICE, verifiedCaller);
        return point.proceed();
    }

    private boolean isMissing(String... values) {
        for (String value : values) {
            if (value == null || value.isBlank()) {
                return true;
            }
        }
        return false;
    }
}
