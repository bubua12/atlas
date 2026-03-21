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
     * Token已过期，请重新登录
     */
    TOKEN_EXPIRED(461, "Token已过期，请重新登录"),
    /**
     * 账户已被锁定
     */
    ACCOUNT_LOCKED(452, "账户已被锁定，请稍后再试"),
    /**
     * IP已被锁定
     */
    IP_LOCKED(451, "IP已被锁定，请稍后再试"),
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
