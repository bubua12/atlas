package com.bubua12.atlas.common.web.handler;

import com.bubua12.atlas.common.core.exception.BusinessErrorCode;
import com.bubua12.atlas.common.core.exception.BusinessException;
import com.bubua12.atlas.common.core.exception.SystemErrorCode;
import com.bubua12.atlas.common.core.result.CommonResult;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器 fixme 目前感觉有点乱，不好把控
 * 统一捕获业务异常、参数校验异常和系统异常，返回标准 CommonResult 响应。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public CommonResult<Void> handleBusinessException(BusinessException e) {
        log.error("业务异常: {}", e.getMessage(), e);
        return CommonResult.fail(e.getErrorCode().getCode(), e.getErrorCode().getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public CommonResult<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldError() != null
                ? e.getBindingResult().getFieldError().getDefaultMessage()
                : SystemErrorCode.PARAM_ERROR.getMessage();
        log.error("参数校验异常: {}", message);
        return CommonResult.fail(SystemErrorCode.PARAM_ERROR.getCode(), message);
    }

    @ExceptionHandler(BindException.class)
    public CommonResult<Void> handleBindException(BindException e) {
        String message = e.getFieldError() != null
                ? e.getFieldError().getDefaultMessage()
                : SystemErrorCode.PARAM_ERROR.getMessage();
        log.error("参数绑定异常: {}", message);
        return CommonResult.fail(SystemErrorCode.PARAM_ERROR.getCode(), message);
    }

    @ExceptionHandler(FeignException.class)
    public CommonResult<Void> handleFeignException(FeignException e) {
        log.error("远程调用异常: {}", e.getMessage(), e);
        return CommonResult.fail(BusinessErrorCode.SYSTEM_ERROR.getCode(), "服务暂不可用，请稍后重试");
    }

    @ExceptionHandler(Exception.class)
    public CommonResult<Void> handleException(Exception e) {
        log.error("系统异常: {}", e.getMessage(), e);
        return CommonResult.fail(BusinessErrorCode.SYSTEM_ERROR.getCode(), BusinessErrorCode.SYSTEM_ERROR.getMessage());
    }
}
