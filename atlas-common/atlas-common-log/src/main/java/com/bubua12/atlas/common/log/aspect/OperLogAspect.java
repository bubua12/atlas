package com.bubua12.atlas.common.log.aspect;

import com.bubua12.atlas.common.log.annotation.OperLog;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 操作日志切面
 * 拦截 @OperLog 注解标注的方法，记录操作标题、耗时和异常信息。
 */
@Slf4j
@Aspect
@Component
public class OperLogAspect {

    @Around("@annotation(operLog)")
    public Object around(ProceedingJoinPoint point, OperLog operLog) throws Throwable {
        String methodName = point.getSignature().toShortString();
        String title = operLog.title();
        String businessType = operLog.businessType();

        log.info("[OperLog] Start - title: {}, businessType: {}, method: {}, args: {}",
                title, businessType, methodName, Arrays.toString(point.getArgs()));

        long startTime = System.currentTimeMillis();
        try {
            Object result = point.proceed();
            long elapsed = System.currentTimeMillis() - startTime;
            log.info("[OperLog] End - title: {}, method: {}, elapsed: {}ms",
                    title, methodName, elapsed);
            return result;
        } catch (Throwable e) {
            long elapsed = System.currentTimeMillis() - startTime;
            log.error("[OperLog] Error - title: {}, method: {}, elapsed: {}ms, error: {}",
                    title, methodName, elapsed, e.getMessage());
            throw e;
        }
    }
}
