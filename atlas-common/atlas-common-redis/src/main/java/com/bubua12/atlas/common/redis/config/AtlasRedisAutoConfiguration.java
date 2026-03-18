package com.bubua12.atlas.common.redis.config;

import com.bubua12.atlas.common.redis.aspect.RedisLimitAspect;
import com.bubua12.atlas.common.redis.service.RedisService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Atlas Redis自动配置类
 * 使用 @AutoConfigureBefore，可以避免和官方自动配置的有冲突
 */
@AutoConfiguration
@AutoConfigureBefore(RedisAutoConfiguration.class)
@Import({RedisConfig.class, RedisLimitAspect.class})
public class AtlasRedisAutoConfiguration {

    @ConditionalOnMissingBean
    @Bean
    public RedisService redisService(RedisTemplate<String, Object> redisTemplate) {
        return new RedisService(redisTemplate);
    }
}
