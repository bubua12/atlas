package com.bubua12.atlas.common.mybatis.annotation;

import java.lang.annotation.*;

/**
 * 数据权限过滤注解
 * 用于 Mapper 方法，自动根据用户角色的数据权限范围过滤查询结果
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {

    /**
     * 部门表的别名
     */
    String deptAlias() default "";

    /**
     * 用户表的别名
     */
    String userAlias() default "";

    /**
     * 部门字段名
     */
    String deptField() default "dept_id";

    /**
     * 用户字段名
     */
    String userField() default "id";
}
