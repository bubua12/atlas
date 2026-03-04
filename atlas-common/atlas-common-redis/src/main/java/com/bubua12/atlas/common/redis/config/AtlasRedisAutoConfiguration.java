package com.bubua12.atlas.common.redis.config;

import com.bubua12.atlas.common.redis.service.RedisService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Atlas Redis自动配置类
 */
@AutoConfiguration
@Import(RedisConfig.class)
public class AtlasRedisAutoConfiguration {

    @ConditionalOnMissingBean
    @Bean
    public RedisService redisService(@Qualifier("customRedisTemplate") RedisTemplate<String, Object> redisTemplate) {
        return new RedisService(redisTemplate);
    }
}
