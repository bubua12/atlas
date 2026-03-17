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
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

/**
 * 操作日志切面
 * 拦截 @OperLog 注解标注的方法，记录操作标题、耗时和异常信息。
 * todo 1、对于敏感字段进行脱敏 2、优化为批量存储 防止高并发下耗尽数据库连接以及占用主要业务资源
 * todo 疑问？这里记录日志的切面要是最里面的？限流肯定在最前面，但是，多个切面共存的时候，如何编排
 * 关于一个方法上的多个切面切面执行逻辑：fixme 如果Order+其他的比如过滤器、拦截器呢？Order的顺序是只管控切面，还是全部都影响？
 * 1、同一个方法调用链里，切面是同步串行的
 * 2、是洋葱模型 （责任链）执行：外层 around → 内层 around → 目标方法 → 反向返回。
 * 3、不加@Order时，谁外谁里不稳定：不是“每次随机函数”，但从框架视角看属于未显式约束的顺序 ，可能受bean注册、自动装配、代理链组装等影响而变化，不应依赖。
 * 3.1 有 @Order / Ordered ：数值越小优先级越高，越靠外层
 * 3.2 无 @Order ：Spring 会按默认比较器和可用元数据排序，但你没给出明确规则时， 业务上应视为不可依赖 。
 * 3.3 所以涉及“短路返回/鉴权/限流/事务/日志”这类对顺序敏感的切面，最好都显式加 @Order。
 * 现在的配置（限流外层、日志内层）就是这个原则的标准用法。
 */
@Slf4j
@Aspect
@Order
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
