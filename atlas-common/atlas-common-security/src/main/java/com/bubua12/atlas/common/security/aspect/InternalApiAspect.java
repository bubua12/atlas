package com.bubua12.atlas.common.security.aspect;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.bubua12.atlas.common.core.constant.SecurityHeaderConstants;
import com.bubua12.atlas.common.core.exception.BusinessErrorCode;
import com.bubua12.atlas.common.core.exception.BusinessException;
import com.bubua12.atlas.common.security.annotation.InternalApi;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

/**
 * Feign接口权限控制切面
 */
@Aspect
@Slf4j
public class InternalApiAspect {

    @Pointcut("@annotation(com.bubua12.atlas.common.security.annotation.InternalApi)")
    public void internalApiPointCut() {
    }

    @Before("internalApiPointCut() && @annotation(internalApi)")
    public void around(InternalApi internalApi) throws Throwable {
        log.info("开始执行Feign接口管控切面逻辑");
        HttpServletRequest request = getCurrentRequest();

        Boolean trustedRequest = (Boolean) request.getAttribute(SecurityHeaderConstants.ATTR_TRUSTED_REQUEST);
        if (!Boolean.TRUE.equals(trustedRequest)) {
            throw new BusinessException(BusinessErrorCode.FORBIDDEN);
        }

        String callerService = (String) request.getAttribute(SecurityHeaderConstants.ATTR_CALLER_SERVICE);
        if (StrUtil.isBlank(callerService)) {
            throw new BusinessException(BusinessErrorCode.FORBIDDEN);
        }

        String[] allowedServices = internalApi.allowedServices();
        if (ArrayUtil.isNotEmpty(allowedServices) && !Arrays.asList(allowedServices).contains(callerService)) {
            log.info("开始抛出异常...");
            throw new BusinessException(BusinessErrorCode.FORBIDDEN);
        }
    }

    private HttpServletRequest getCurrentRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (!(requestAttributes instanceof ServletRequestAttributes servletRequestAttributes)) {
            throw new BusinessException(BusinessErrorCode.FORBIDDEN);
        }
        return servletRequestAttributes.getRequest();
    }
}
