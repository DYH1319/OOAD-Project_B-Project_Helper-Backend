package cn.edu.sustech.ooadbackend.exception;

import cn.edu.sustech.ooadbackend.common.BaseResponse;
import cn.edu.sustech.ooadbackend.common.StatusCode;
import cn.edu.sustech.ooadbackend.utils.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * @author DYH
 * @version 1.0
 * @className GlobalExceptionHandler
 * @since 2023/10/9 15:35
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException e) {
        log.warn("businessException: " + e.getMessage() + ": " + e.getDescription());
        return ResponseUtils.fail(e.getStatusCode(), e.getDescription());
    }

    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> runtimeExceptionHandler(RuntimeException e) {
        log.error("runtimeException: " + e.getMessage() + e);
        return ResponseUtils.fail(StatusCode.SYSTEM_ERROR, StatusCode.SYSTEM_ERROR.getMessage());
    }
}
