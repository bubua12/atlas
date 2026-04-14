package com.bubua12.atlas.system.security;

import com.bubua12.atlas.api.auth.constant.AuthCacheConstant;
import com.bubua12.atlas.api.auth.event.PermissionChangeEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 发布权限变更通知，驱动在线会话刷新。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PermissionChangePublisher {

    private final StringRedisTemplate stringRedisTemplate;

    private final ObjectMapper objectMapper;

    public void publishUsersChanged(Collection<Long> userIds, String reason) {
        Set<Long> affectedUserIds = normalize(userIds);
        if (affectedUserIds.isEmpty()) {
            return;
        }

        Runnable publishAction = () -> doPublish(buildEvent(affectedUserIds, reason));
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publishAction.run();
                }
            });
            return;
        }

        publishAction.run();
    }

    private PermissionChangeEvent buildEvent(Set<Long> userIds, String reason) {
        PermissionChangeEvent event = new PermissionChangeEvent();
        event.setUserIds(userIds);
        event.setReason(reason);
        event.setTimestamp(System.currentTimeMillis());
        return event;
    }

    private void doPublish(PermissionChangeEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            stringRedisTemplate.convertAndSend(AuthCacheConstant.AUTH_PERMISSION_CHANGE_CHANNEL, payload);
            log.info("发布权限变更事件，reason={}, userIds={}", event.getReason(), event.getUserIds());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("序列化权限变更事件失败", e);
        }
    }

    private Set<Long> normalize(Collection<Long> userIds) {
        Set<Long> normalized = new LinkedHashSet<>();
        if (userIds == null) {
            return normalized;
        }
        for (Long userId : userIds) {
            if (userId != null) {
                normalized.add(userId);
            }
        }
        return normalized;
    }
}
