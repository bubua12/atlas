package com.bubua12.atlas.common.log.aspect;

import com.bubua12.atlas.common.core.context.SecurityContextHolder;
import com.bubua12.atlas.common.log.annotation.OperLog;
import com.bubua12.atlas.common.log.entity.SysOperLog;
import com.bubua12.atlas.common.log.service.AsyncOperLogService;
import com.bubua12.atlas.common.web.util.IpUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

/**
 * 操作日志切面
 * 拦截 @OperLog 注解标注的方法，记录操作标题、耗时和异常信息。
 * todo 1、对于敏感字段进行脱敏 2、优化为批量存储 防止高并发下耗尽数据库连接以及占用主要业务资源
 */
@Slf4j
@Aspect
@Component
public class OperLogAspect {

    // 注入异步服务 防止自调用导致异步失效
    @Resource
    private AsyncOperLogService asyncOperLogService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 未获取到当前操作人的情况
    public static final String ANONYMOUS_NAME = "anonymous";

    @Around("@annotation(operLog)")
    public Object around(ProceedingJoinPoint point, OperLog operLog) throws Throwable {
        SysOperLog operLogEntity = new SysOperLog();

        operLogEntity.setTitle(operLog.title());
        operLogEntity.setBusinessType(operLog.businessType());
        operLogEntity.setMethod(point.getSignature().toLongString());
        operLogEntity.setOperTime(LocalDateTime.now());

        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            operLogEntity.setRequestMethod(request.getMethod());
            operLogEntity.setOperIp(IpUtils.getIpAddr(request));
        }

        // 获取操作人
        try {
            String username = SecurityContextHolder.getUsername();
            operLogEntity.setOperName(username != null ? username : ANONYMOUS_NAME);
        } catch (Exception e) {
            operLogEntity.setOperName(ANONYMOUS_NAME);
        }

        // 获取请求参数
        try {
            String params = objectMapper.writeValueAsString(point.getArgs());
            operLogEntity.setOperParam(params.length() > 2000 ? params.substring(0, 2000) : params);
        } catch (Exception e) {
            operLogEntity.setOperParam("参数序列化失败");
        }

        long startTime = System.currentTimeMillis();
        try {
            Object result = point.proceed();
            operLogEntity.setCostTime(System.currentTimeMillis() - startTime);
            operLogEntity.setStatus(0);

            // 记录返回结果
            try {
                String jsonResult = objectMapper.writeValueAsString(result);
                operLogEntity.setJsonResult(jsonResult.length() > 2000 ? jsonResult.substring(0, 2000) : jsonResult);
            } catch (Exception e) {
                operLogEntity.setJsonResult("结果序列化失败");
            }

            asyncOperLogService.saveLog(operLogEntity);
            return result;
        } catch (Throwable e) {
            operLogEntity.setCostTime(System.currentTimeMillis() - startTime);
            operLogEntity.setStatus(1);
            operLogEntity.setErrorMsg(e.getMessage() != null && e.getMessage().length() > 2000
                    ? e.getMessage().substring(0, 2000) : e.getMessage());
            asyncOperLogService.saveLog(operLogEntity);
            throw e;
        }
    }
}
