package com.bubua12.atlas.common.core.exception;

import com.bubua12.atlas.common.core.exception.code.ErrorCode;

/**
 *
 *
 * @author bubua12
 * @since 2026/3/18 9:24
 */
public enum SystemErrorCode implements ErrorCode {
    PARAM_ERROR(400, "参数错误");

    private final int code;

    private final String message;

    SystemErrorCode(int code, String message) {
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
