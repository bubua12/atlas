package com.bubua12.atlas.common.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记仅供内部服务调用的接口
 * 配合 InternalApiAspect 使用，验证调用方服务名
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface InternalApi {
    /**
     * 允许调用的服务名列表，空表示所有内部服务均可调用
     */
    String[] allowedServices() default {};
}
