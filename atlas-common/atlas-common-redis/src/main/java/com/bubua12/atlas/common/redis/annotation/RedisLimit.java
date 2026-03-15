package com.bubua12.atlas.common.redis.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口限流注解
 *
 * @author bubua12
 * @since 2026/03/13 23:34
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface RedisLimit {

    /**
     * 资源的唯一名称
     * 作用：不同的接口，不同的流量控制
     */
    String key() default "";

    /**
     * 最多的访问限制次数
     */
    long permitsPerSecond() default 2;

    /**
     * 过期时间，也可以理解为单位时间或滑动时间窗口，单位：秒。默认值：60
     */
    long expire() default 60;

    /**
     * 得不到令牌的提示语 或者 限流提示词
     */
    String msg() default "操作过于频繁，请稍后再试，谢谢";
}
