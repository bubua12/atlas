package com.bubua12.atlas.common.redis.exception;

/**
 * Redis限流异常
 *
 * @author bubua12
 * @since 2026/03/13 23:37
 */
public class RedisLimitException extends RuntimeException {

    public RedisLimitException(String message) {
        super(message);
    }
}
