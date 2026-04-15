package com.bubua12.atlas.common.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记仅允许内部服务调用的接口。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface InternalApi {

    /**
     * 允许访问该接口的内部服务列表。
     * 空数组表示任意通过签名校验的内部服务均可访问。
     */
    String[] allowedServices() default {};
}
