package com.bubua12.atlas.common.security.aspect;

import com.bubua12.atlas.common.core.exception.BusinessErrorCode;
import com.bubua12.atlas.common.core.exception.BusinessException;
import com.bubua12.atlas.common.security.annotation.InternalApi;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

/**
 * 仅供内部服务调用的接口切面验证，验证调用方服务名
 */
@Aspect
@Component
@Slf4j
public class InternalApiAspect {

    /**
     * 注意不是所有的切面都是环绕，这个就是前置校验即可
     */
    @Before("@annotation(internalApi)")
    public void before(InternalApi internalApi) {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            throw new BusinessException(BusinessErrorCode.FORBIDDEN);
        }

        String callerService = attrs.getRequest().getHeader("X-Service-Name");
        if (null == callerService) {
            throw new BusinessException(BusinessErrorCode.FORBIDDEN);
        }

        String[] allowedServices = internalApi.allowedServices();
        if (allowedServices.length > 0 && !Arrays.asList(allowedServices).contains(callerService)) {
            log.warn("服务 {} 无权调用此内部接口", callerService);
            throw new BusinessException(BusinessErrorCode.FORBIDDEN);
        }
    }
}
