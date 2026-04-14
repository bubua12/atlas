package com.bubua12.atlas.common.security.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记仅允许内部服务调用的接口。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface InternalApi {

    /**
     * 允许访问的服务列表，空表示任意已验签的内部服务。
     */
    String[] allowedServices() default {};
}
