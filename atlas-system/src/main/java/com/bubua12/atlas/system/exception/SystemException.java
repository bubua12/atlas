package com.bubua12.atlas.system.exception;

import com.bubua12.atlas.common.core.exception.BusinessException;
import com.bubua12.atlas.common.core.exception.code.ErrorCode;

/**
 *
 * @author bubua12
 * @since 2026/3/17 22:51
 */
public class SystemException extends BusinessException {

    public SystemException(ErrorCode errorCode) {
        super(errorCode);
    }

    public SystemException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }

    public SystemException(String message, Throwable cause, ErrorCode errorCode) {
        super(message, cause, errorCode);
    }

    public SystemException(Throwable cause, ErrorCode errorCode) {
        super(cause, errorCode);
    }

    public SystemException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, ErrorCode errorCode) {
        super(message, cause, enableSuppression, writableStackTrace, errorCode);
    }
}
