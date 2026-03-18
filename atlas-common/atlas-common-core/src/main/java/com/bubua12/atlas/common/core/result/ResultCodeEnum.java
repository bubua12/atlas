package com.bubua12.atlas.common.core.result;

import lombok.Getter;

import java.util.Arrays;

/**
 * 通用响应状态码枚举
 *
 * @author bubua12
 * @since 2024-05-24 08:59
 */
@Getter
public enum ResultCodeEnum {
    /**
     * 操作失败
     **/
    RC999(999, "操作失败"),
    /**
     * 操作成功
     **/
    RC200(200, "success"),
    /**
     * 服务降级
     **/
    RC201(201, "服务开启降级保护,请稍后再试!"),
    /**
     * 热点参数限流
     **/
    RC202(202, "热点参数限流,请稍后再试!"),
    /**
     * 系统规则不满足
     **/
    RC203(203, "系统规则不满足要求,请稍后再试!"),
    /**
     * 授权规则不通过
     **/
    RC204(204, "授权规则不通过,请稍后再试!"),
    /**
     * access_denied
     **/
    RC403(403, "无访问权限,请联系管理员授予权限"),
    /**
     * access_denied
     **/
    RC401(401, "匿名用户访问无权限资源时的异常"),
    RC404(404, "404页面找不到的异常"),
    /**
     * 服务异常
     **/
    RC500(500, "系统异常，请稍后重试"),
    RC375(375, "数学运算异常，请稍后重试"),

    INVALID_TOKEN(2001, "访问令牌不合法"),
    ACCESS_DENIED(2003, "没有权限访问该资源"),
    CLIENT_AUTHENTICATION_FAILED(1001, "客户端认证失败"),
    USERNAME_OR_PASSWORD_ERROR(1002, "用户名或密码错误"),
    BUSINESS_ERROR(1004, "业务逻辑异常"),
    REQPARAM_NEGATIVE(1005, "请求参数不能是负数"),
    UNSUPPORTED_GRANT_TYPE(1003, "不支持的认证模式");

    /**
     * 自定义状态码
     **/
    private final int code;
    /**
     * 自定义描述
     **/
    private final String message;

    ResultCodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    // 遍历枚举V1 fixme 这里的这个方法的作用，以及整体code的数据类型的设计，包括==判断的漏洞
    public static ResultCodeEnum getResponseCodeEnum(int code) {
        for (ResultCodeEnum element : ResultCodeEnum.values()) {
            if (element.getCode() == code) {
                return element;
            }
        }
        return null;
    }

    // 遍历枚举V2
    public static ResultCodeEnum getResponseCodeEnumV2(int code) {
        return Arrays.stream(ResultCodeEnum.values()).filter(x -> x.getCode() == code).findFirst().orElse(null);
    }
}