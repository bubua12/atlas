package com.bubua12.atlas.common.core.result;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 通用返回增强
 *
 * @param <T> 泛型
 */
@Data
public class CommonResult<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private int code;
    private String msg;
    private T data;

    /**
     * 链路追踪
     */
    private String traceId;

    private CommonResult() {
    }

    private CommonResult(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> CommonResult<T> success() {
        return new CommonResult<>(ResultCodeEnum.RC200.getCode(), ResultCodeEnum.RC200.getMessage(), null);
    }

    public static <T> CommonResult<T> success(T data) {
        return new CommonResult<>(ResultCodeEnum.RC200.getCode(), ResultCodeEnum.RC200.getMessage(), data);
    }

    public static <T> CommonResult<T> fail(String msg) {
        return new CommonResult<>(ResultCodeEnum.RC500.getCode(), msg, null);
    }

    public static <T> CommonResult<T> fail(int code, String msg) {
        return new CommonResult<>(code, msg, null);
    }
}
