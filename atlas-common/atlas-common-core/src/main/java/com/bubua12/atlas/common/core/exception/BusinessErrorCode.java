package com.bubua12.atlas.common.core.exception;

import com.bubua12.atlas.common.core.exception.code.ErrorCode;

/**
 *
 *
 * @author bubua12
 * @since 2026/3/18 9:24
 */
public enum BusinessErrorCode implements ErrorCode {
    SYSTEM_ERROR(500, "系统异常，请联系开发人员"),
    FORBIDDEN(403, "无权限"),
    UNAUTHORIZED(401, "未登录或登录已过期");

    private final int code;

    private final String message;

    BusinessErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int getCode() {
        return this.code;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
