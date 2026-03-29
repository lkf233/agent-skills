package org.lkf.agent.common.exception;

import org.lkf.agent.common.dto.ApiResponseObject;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ApiResponseObject<Void> handleBusinessException(BusinessException exception) {
        return ApiResponseObject.fail(exception.getCode(), exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponseObject<Void> handleValidationException(MethodArgumentNotValidException exception) {
        if (exception.getBindingResult().getFieldError() != null) {
            return ApiResponseObject.fail(400, exception.getBindingResult().getFieldError().getDefaultMessage());
        }
        return ApiResponseObject.fail(400, "参数校验失败");
    }

    @ExceptionHandler(Exception.class)
    public ApiResponseObject<Void> handleException(Exception exception) {
        return ApiResponseObject.fail(500, exception.getMessage());
    }
}
