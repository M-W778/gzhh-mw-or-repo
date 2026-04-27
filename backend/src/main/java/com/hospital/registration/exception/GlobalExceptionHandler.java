package com.hospital.registration.exception;

import com.hospital.registration.common.ErrorCode;
import com.hospital.registration.common.ResultUtils;
import com.hospital.registration.common.BaseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> handleBusinessException(BusinessException ex) {
        return ResultUtils.error(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public BaseResponse<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResultUtils.error(ErrorCode.PARAMS_ERROR, "validation failed: " + errors);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public BaseResponse<?> handleBadCredentialsException(BadCredentialsException ex) {
        return ResultUtils.error(ErrorCode.NOT_LOGIN_ERROR, "invalid account or password");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public BaseResponse<?> handleAccessDeniedException(AccessDeniedException ex) {
        return ResultUtils.error(ErrorCode.NO_AUTH_ERROR, "forbidden");
    }

    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> handleRuntimeException(RuntimeException ex) {
        return ResultUtils.error(ErrorCode.OPERATION_ERROR, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public BaseResponse<?> handleException(Exception ex) {
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "internal error: " + ex.getMessage());
    }
}
