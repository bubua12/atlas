package com.bubua12.atlas.common.security.aspect;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.bubua12.atlas.common.core.context.SecurityContextHolder;
import com.bubua12.atlas.common.core.exception.BusinessErrorCode;
import com.bubua12.atlas.common.core.exception.BusinessException;
import com.bubua12.atlas.common.core.model.LoginUser;
import com.bubua12.atlas.common.redis.service.RedisService;
import com.bubua12.atlas.common.security.annotation.RequiresPermission;
import com.bubua12.atlas.common.security.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 权限校验切面。
 *
 * <p>它只负责“当前登录用户是否拥有某个权限标识”，
 * 不负责证明请求是谁发起的。
 * 请求身份的真实性来自网关验签、内部服务认证或 token/Redis 登录态恢复。
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class PreAuthorizeAspect {

    private static final String TOKEN_CACHE_PREFIX = "auth:token:";

    private final RedisService redisService;
    private final JwtUtils jwtUtils;

    @Pointcut("@annotation(com.bubua12.atlas.common.security.annotation.RequiresPermission)")
    public void permissionPointCut() {
    }

    @Around("permissionPointCut() && @annotation(requiresPermission)")
    public Object around(ProceedingJoinPoint point, RequiresPermission requiresPermission) throws Throwable {
        LoginUser loginUser = SecurityContextHolder.getLoginUser();
        if (loginUser == null) {
            // 灰度期间保留 token -> Redis 的旧链路回退，便于逐步切到“网关验签后直接建上下文”。
            String token = SecurityContextHolder.getToken();
            if (StrUtil.isBlank(token)) {
                throw new BusinessException(BusinessErrorCode.UNAUTHORIZED);
            }

            String rawToken = jwtUtils.normalizeToken(token);
            if (StrUtil.isBlank(rawToken)) {
                throw new BusinessException(BusinessErrorCode.UNAUTHORIZED);
            }

            loginUser = redisService.get(TOKEN_CACHE_PREFIX + rawToken);
        }

        if (loginUser == null) {
            throw new BusinessException(BusinessErrorCode.UNAUTHORIZED);
        }

        Long userId = loginUser.getUserId();
        if (Long.valueOf(1L).equals(userId)) {
            // 超级管理员约定继续保留，避免影响既有权限模型。
            return point.proceed();
        }

        String requiredPerm = requiresPermission.value();
        if (StrUtil.isBlank(requiredPerm)) {
            // 没声明具体权限时不做额外限制，保持注解语义简单直接。
            return point.proceed();
        }

        Set<String> userPerms = loginUser.getPermissions();
        if (CollectionUtil.isEmpty(userPerms) || !userPerms.contains(requiredPerm)) {
            log.warn("用户 {} 权限不足，需要权限 {}，已有权限 {}", userId, requiredPerm, userPerms);
            throw new BusinessException(BusinessErrorCode.FORBIDDEN);
        }

        return point.proceed();
    }
}
