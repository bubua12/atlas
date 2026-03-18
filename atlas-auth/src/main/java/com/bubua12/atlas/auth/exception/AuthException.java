package com.bubua12.atlas.auth.exception;

import com.bubua12.atlas.common.core.exception.BusinessException;
import com.bubua12.atlas.common.core.exception.code.ErrorCode;

/**
 * fixme 这里只是这样写，对于错误提示信息不友好，后续优化
 *
 * @author bubua12
 * @since 2026/3/17 22:51
 */
public class AuthException extends BusinessException {

    public AuthException(ErrorCode errorCode) {
        super(errorCode);
    }

    public AuthException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }

    public AuthException(String message, Throwable cause, ErrorCode errorCode) {
        super(message, cause, errorCode);
    }

    public AuthException(Throwable cause, ErrorCode errorCode) {
        super(cause, errorCode);
    }

    public AuthException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, ErrorCode errorCode) {
        super(message, cause, enableSuppression, writableStackTrace, errorCode);
    }
}
