package com.bubua12.atlas.auth.config;

import com.bubua12.atlas.api.auth.constant.AuthCacheConstant;
import com.bubua12.atlas.auth.listener.PermissionChangeListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
public class RedisPubSubConfig {

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory, PermissionChangeListener permissionChangeListener) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        // system 只负责发布“谁的登录态需要刷新”，auth 订阅后在本地更新 token 缓存，避免反向服务调用。
        container.addMessageListener(permissionChangeListener, new ChannelTopic(AuthCacheConstant.PERMISSION_CHANGE_CHANNEL));
        return container;
    }
}
