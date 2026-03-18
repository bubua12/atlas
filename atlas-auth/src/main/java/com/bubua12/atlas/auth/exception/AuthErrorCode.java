package com.bubua12.atlas.auth.exception;

import com.bubua12.atlas.common.core.exception.code.ErrorCode;

/**
 *
 *
 * @author bubua12
 * @since 2026/3/18 9:12
 */
public enum AuthErrorCode implements ErrorCode {

    /**
     * 用户名或密码错误
     */
    USERNAME_OR_PASSWORD_WRONG(450, "用户名或密码错误"),
    /**
     * 验证码错误
     */
    VERIFICATION_CODE_WRONG(460, "验证码错误");

    private final int code;

    private final String message;

    AuthErrorCode(int code, String message) {
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
