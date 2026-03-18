package com.bubua12.atlas.common.security.aspect;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.bubua12.atlas.common.core.context.SecurityContextHolder;
import com.bubua12.atlas.common.core.exception.BusinessErrorCode;
import com.bubua12.atlas.common.core.exception.BusinessException;
import com.bubua12.atlas.common.core.model.LoginUser;
import com.bubua12.atlas.common.redis.service.RedisService;
import com.bubua12.atlas.common.security.annotation.RequiresPermission;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 权限校验切面
 * 拦截 @RequiresPermission 注解，校验用户权限
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class PreAuthorizeAspect {

    private final RedisService redisService;

    // Redis key 前缀，需与 auth 服务保持一致
    private static final String TOKEN_CACHE_PREFIX = "auth:token:";

    @Pointcut("@annotation(com.bubua12.atlas.common.security.annotation.RequiresPermission)")
    public void permissionPointCut() {
    }

    @Around("permissionPointCut() && @annotation(requiresPermission)")
    public Object around(ProceedingJoinPoint point, RequiresPermission requiresPermission) throws Throwable {
        // 1. 获取当前 Token (从 SecurityContextHolder)
        String token = SecurityContextHolder.getToken();
        if (StrUtil.isBlank(token)) {
            // 尝试从 UserContextInterceptor 设置的 userId 判断是否已登录，但没有 Token 无法查 Redis 缓存
            throw new BusinessException(BusinessErrorCode.UNAUTHORIZED);
        }

        // 去掉 Bearer 前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // 2. 从 Redis 获取 LoginUser
        LoginUser loginUser = redisService.get(TOKEN_CACHE_PREFIX + token);
        if (loginUser == null) {
            throw new BusinessException(BusinessErrorCode.UNAUTHORIZED);
        }

        Long userId = loginUser.getUserId();
        // 超级管理员直接放行
        if (Long.valueOf(1L).equals(userId)) {
            return point.proceed();
        }

        // 3. 校验权限
        String requiredPerm = requiresPermission.value();
        if (StrUtil.isBlank(requiredPerm)) {
            return point.proceed();
        }

        Set<String> userPerms = loginUser.getPermissions();

        if (CollectionUtil.isEmpty(userPerms) || !userPerms.contains(requiredPerm)) {
            log.warn("用户 {} 权限不足，需要权限: {}，已有权限: {}", userId, requiredPerm, userPerms);
            throw new BusinessException(BusinessErrorCode.FORBIDDEN);
        }

        return point.proceed();
    }
}
