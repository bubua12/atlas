package com.bubua12.atlas.common.core.exception.code;

/**
 * 统一异常处理体系 错误码
 * 具体地实现其实是一个个枚举，通过枚举约束所有的错误码
 *
 * @author bubua12
 * @since 2025/10/10 10:14
 */
public interface ErrorCode {

    /**
     * 错误码
     *
     * @return code
     */
    int getCode();

    /**
     * 错误信息
     *
     * @return message
     */
    String getMessage();
}
