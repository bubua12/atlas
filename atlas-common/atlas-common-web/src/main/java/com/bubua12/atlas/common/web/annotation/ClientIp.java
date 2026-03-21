package com.bubua12.atlas.common.web.annotation;

import java.lang.annotation.*;

/**
 * 标注在方法参数上，自动注入客户端真实IP地址
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ClientIp {
}