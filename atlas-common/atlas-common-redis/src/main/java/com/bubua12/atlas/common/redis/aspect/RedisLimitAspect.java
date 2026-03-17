package com.bubua12.atlas.common.redis.aspect;


import com.bubua12.atlas.common.core.result.CommonResult;
import com.bubua12.atlas.common.redis.annotation.RedisLimit;
import com.bubua12.atlas.common.redis.exception.RedisLimitException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 接口限流切面逻辑
 *
 * @author bubua12
 * @since 2026/03/13 23:35
 */
@Slf4j
@Aspect
@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
public class RedisLimitAspect {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private DefaultRedisScript<Long> redisLuaScript;

    /**
     * 优先从类路径中加载Lua脚本
     */
    @PostConstruct
    public void init() {
        redisLuaScript = new DefaultRedisScript<>();
        redisLuaScript.setResultType(Long.class);
        redisLuaScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("rateLimiter.lua")));
    }

    @Pointcut(value = "@annotation(com.bubua12.atlas.common.redis.annotation.RedisLimit)")
    public void pointCut() {
    }

    @Around("pointCut()")
    public Object around(ProceedingJoinPoint pjp) {
        Object result;

        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();

        RedisLimit annotation = method.getAnnotation(RedisLimit.class);
        if (null != annotation) {
            // 获取注解上标注的数据
            String key = annotation.key();

            if (null == key) {
                throw new RedisLimitException("限流注解key不能为空值");
            }

            String className = method.getDeclaringClass().getName();
            String methodName = method.getName();

            // fixme 这里连接符需要注意为啥原生的是制表符
            String limitKey = className + ":" + methodName + ":" + key;
            log.info("Redis限流注解信息，限流的key: {}", limitKey);

            long limit = annotation.permitsPerSecond();
            long expire = annotation.expire();

            List<String> redisLuaScriptKeysList = new ArrayList<>();
            redisLuaScriptKeysList.add(limitKey);

            // fixme 这里原生数据类型也不报错，但是执行报错类型转换异常
            // 系统异常: class java.lang.Long cannot be cast to class java.lang.String (java.lang.Long and java.lang.String are in module java.base of loader 'bootstrap')
            Long count = stringRedisTemplate.execute(redisLuaScript, redisLuaScriptKeysList,
                    String.valueOf(limit), String.valueOf(expire));
            log.debug("当前接口 {} 访问次数 {} ", limitKey, count);
            if (count == 0) {
                log.debug("方法 {} 触发接口限流", methodName);
                return CommonResult.fail(annotation.msg());
            }
        }

        try {
            // 放行
            result = pjp.proceed();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        log.info("方法限流通过，执行结束啦");

        return result;
    }
}
