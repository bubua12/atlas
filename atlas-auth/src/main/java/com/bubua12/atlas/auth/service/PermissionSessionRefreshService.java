package com.bubua12.atlas.auth.service;

import java.util.Set;

/**
 * 在线会话权限刷新服务。
 */
public interface PermissionSessionRefreshService {

    /**
     * 刷新指定用户的在线会话。
     *
     * @param userIds 用户ID集合
     */
    void refreshUserSessions(Set<Long> userIds);
}
