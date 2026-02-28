package com.bubua12.atlas.common.log.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 * 标注在 Controller 方法上，由 {@link com.bubua12.atlas.common.log.aspect.OperLogAspect} 切面记录操作日志。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperLog {

    /**
     * 操作标题
     */
    String title() default "";

    /**
     * 业务类型（如 新增、修改、删除）
     */
    String businessType() default "";
}
