package com.bubua12.atlas.auth.config;

import com.bubua12.atlas.api.auth.constant.AuthCacheConstant;
import com.bubua12.atlas.auth.listener.PermissionChangeSubscriber;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * 权限变更事件监听配置。
 */
@Configuration
public class PermissionChangeListenerConfig {

    @Bean
    public RedisMessageListenerContainer permissionChangeListenerContainer(RedisConnectionFactory connectionFactory,
                                                                           PermissionChangeSubscriber subscriber) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(subscriber, new ChannelTopic(AuthCacheConstant.AUTH_PERMISSION_CHANGE_CHANNEL));
        return container;
    }
}
