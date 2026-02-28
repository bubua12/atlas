package com.bubua12.atlas.common.core.exception;

import lombok.Getter;

import java.io.Serial;

/**
 * 业务异常
 * 用于业务逻辑校验失败时抛出，由全局异常处理器统一捕获返回。
 */
@Getter
public class BusinessException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final int code;

    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
