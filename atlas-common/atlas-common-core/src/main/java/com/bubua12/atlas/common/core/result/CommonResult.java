package com.bubua12.atlas.common.core.result;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class CommonResult<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private int code;
    private String msg;
    private T data;

    private CommonResult() {
    }

    private CommonResult(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> CommonResult<T> ok() {
        return new CommonResult<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg(), null);
    }

    public static <T> CommonResult<T> ok(T data) {
        return new CommonResult<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg(), data);
    }

    public static <T> CommonResult<T> fail(String msg) {
        return new CommonResult<>(ResultCode.FAILED.getCode(), msg, null);
    }

    public static <T> CommonResult<T> fail(int code, String msg) {
        return new CommonResult<>(code, msg, null);
    }
}
