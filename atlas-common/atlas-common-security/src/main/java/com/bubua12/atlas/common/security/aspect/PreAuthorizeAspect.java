package com.bubua12.atlas.common.security.aspect;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.bubua12.atlas.common.core.context.SecurityContextHolder;
import com.bubua12.atlas.common.core.exception.BusinessErrorCode;
import com.bubua12.atlas.common.core.exception.BusinessException;
import com.bubua12.atlas.common.core.model.LoginUser;
import com.bubua12.atlas.common.core.utils.TokenUtils;
import com.bubua12.atlas.common.redis.service.RedisService;
import com.bubua12.atlas.common.security.annotation.RequiresPermission;
import com.bubua12.atlas.common.security.constant.PermissionConstants;
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
        // 1. 优先复用网关透传并验签后的上下文，避免重复查询 Redis
        LoginUser loginUser = SecurityContextHolder.getLoginUser();
        if (loginUser == null) {
            String token = TokenUtils.resolveToken(SecurityContextHolder.getToken());
            if (StrUtil.isBlank(token)) {
                throw new BusinessException(BusinessErrorCode.UNAUTHORIZED);
            }
            loginUser = redisService.get(TOKEN_CACHE_PREFIX + token);
            if (loginUser != null) {
                SecurityContextHolder.setLoginUser(loginUser);
            }
        }

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

        if (!hasPermission(userPerms, requiredPerm)) {
            log.warn("用户 {} 权限不足，需要权限: {}，已有权限: {}", userId, requiredPerm, userPerms);
            throw new BusinessException(BusinessErrorCode.FORBIDDEN);
        }

        return point.proceed();
    }

    private boolean hasPermission(Set<String> userPerms, String requiredPerm) {
        if (CollectionUtil.isEmpty(userPerms)) {
            return false;
        }
        if (userPerms.contains(requiredPerm) || userPerms.contains(PermissionConstants.ALL_PERMISSION)) {
            return true;
        }
        return userPerms.stream()
                .filter(StrUtil::isNotBlank)
                .anyMatch(permission -> matchesWildcard(permission, requiredPerm));
    }

    private boolean matchesWildcard(String ownedPermission, String requiredPermission) {
        String[] ownedParts = StrUtil.splitToArray(ownedPermission, ':');
        String[] requiredParts = StrUtil.splitToArray(requiredPermission, ':');

        if (ownedParts.length != requiredParts.length) {
            return false;
        }

        for (int i = 0; i < ownedParts.length; i++) {
            if (!"*".equals(ownedParts[i]) && !StrUtil.equals(ownedParts[i], requiredParts[i])) {
                return false;
            }
        }
        return true;
    }
}
