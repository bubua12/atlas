package com.bubua12.atlas.auth.config;

import com.bubua12.atlas.auth.listener.PermissionChangeListener;
import com.bubua12.atlas.common.redis.pubsub.PermissionChangePublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
public class RedisListenerConfig {

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            PermissionChangeListener permissionChangeListener) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(permissionChangeListener,
                new ChannelTopic(PermissionChangePublisher.CHANNEL));
        return container;
    }
}
