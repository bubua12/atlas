package com.bubua12.atlas.common.core.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    FAILED(500, "操作失败"),
    UNAUTHORIZED(401, "未认证"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    PARAM_ERROR(400, "参数错误");

    private final int code;
    private final String msg;
}
