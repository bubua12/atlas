package com.bubua12.atlas.common.opentelemetry.aspect;

import com.bubua12.atlas.common.core.result.CommonResult;
import com.bubua12.starter.config.tracer.TraceUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 *
 *
 * @author bubua12
 * @since 2026/3/26 10:36
 */
@Aspect
@Component
public class CommonResultTraceAspect {

    @Pointcut("execution(* com.bubua12..*Controller.*(..)) || @within(org.springframework.web.bind.annotation.RestControllerAdvice)")
    public void pointCut() {
    }

    @Around("pointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result;

        result = joinPoint.proceed(); // 放行

        if (result instanceof CommonResult) {
            ((CommonResult<?>) result).setTraceId(TraceUtil.getTraceId());
        }

        return result;
    }

}
