package com.bubua12.atlas.common.redis.utils;

import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * Redis 工具类
 *
 * @author bubua12
 * @since 2026/03/21 22:54
 */
public class AtlasRedisUtils {

    private AtlasRedisUtils() {
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static byte[] serialize(RedisSerializer serializer, Object object) {
        return serializer.serialize(object);
    }
}
