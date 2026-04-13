package com.bubua12.atlas.common.security.aspect;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.bubua12.atlas.common.core.context.SecurityContextHolder;
import com.bubua12.atlas.common.core.exception.BusinessErrorCode;
import com.bubua12.atlas.common.core.exception.BusinessException;
import com.bubua12.atlas.common.core.model.LoginUser;
import com.bubua12.atlas.common.security.annotation.RequiresPermission;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Set;

/**
 * 权限校验切面
 * 拦截 @RequiresPermission 注解，校验用户权限
 */
@Aspect
@Component
@Slf4j
public class PreAuthorizeAspect {

    @Pointcut("@annotation(com.bubua12.atlas.common.security.annotation.RequiresPermission)")
    public void permissionPointCut() {
    }

    @Around("permissionPointCut() && @annotation(requiresPermission)")
    public Object around(ProceedingJoinPoint point, RequiresPermission requiresPermission) throws Throwable {
        // 1. 获取当前用户 ID
        Long userId = SecurityContextHolder.getUserId();
        if (userId == null) {
            throw new BusinessException(BusinessErrorCode.UNAUTHORIZED);
        }

        // 超级管理员直接放行
        if (Long.valueOf(1L).equals(userId)) {
            return point.proceed();
        }

        // 2. 校验权限
        String requiredPerm = requiresPermission.value();
        if (StrUtil.isBlank(requiredPerm)) {
            return point.proceed();
        }

        // 3. 从请求属性获取 LoginUser（由网关传递，无需查询 Redis）
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            throw new BusinessException(BusinessErrorCode.UNAUTHORIZED);
        }
        LoginUser loginUser = (LoginUser) attrs.getRequest().getAttribute("loginUser");
        if (loginUser == null) {
            throw new BusinessException(BusinessErrorCode.UNAUTHORIZED);
        }

        Set<String> userPerms = loginUser.getPermissions();
        if (CollectionUtil.isEmpty(userPerms)) {
            log.info("用户 {} 权限不足，需要权限: {}，已有权限: {}", userId, requiredPerm, userPerms);
            throw new BusinessException(BusinessErrorCode.FORBIDDEN);
        }

        // 超级管理员通配符直接放行
        if (userPerms.contains("*:*:*")) {
            return point.proceed();
        }

        if (!userPerms.contains(requiredPerm)) {
            log.info("用户 {} 权限不足，需要权限: {}，已有权限: {}", userId, requiredPerm, userPerms);
            throw new BusinessException(BusinessErrorCode.FORBIDDEN);
        }

        return point.proceed();
    }
}
