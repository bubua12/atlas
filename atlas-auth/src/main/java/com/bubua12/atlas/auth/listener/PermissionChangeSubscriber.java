package com.bubua12.atlas.auth.listener;

import com.bubua12.atlas.api.auth.event.PermissionChangeEvent;
import com.bubua12.atlas.auth.service.PermissionSessionRefreshService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * 订阅权限变更事件并刷新在线会话。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PermissionChangeSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;

    private final PermissionSessionRefreshService permissionSessionRefreshService;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String payload = new String(message.getBody(), StandardCharsets.UTF_8);
            PermissionChangeEvent event = objectMapper.readValue(payload, PermissionChangeEvent.class);
            log.info("收到权限变更事件，reason={}, userIds={}", event.getReason(), event.getUserIds());
            permissionSessionRefreshService.refreshUserSessions(event.getUserIds());
        } catch (Exception e) {
            log.error("处理权限变更事件失败", e);
        }
    }
}
