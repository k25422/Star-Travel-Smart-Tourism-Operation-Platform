package com.example.tourism.controller;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleIllegalArgument(IllegalArgumentException ex) {
        // 业务异常统一返回 400，并给前端一个 message 字段。
        Map<String, String> body = new HashMap<String, String>();
        body.put("message", ex.getMessage());
        return body;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidation(MethodArgumentNotValidException ex) {
        // 参数校验失败时，比如出行人数小于 1，也统一返回 400。
        Map<String, String> body = new HashMap<String, String>();
        FieldError fieldError = ex.getBindingResult().getFieldError();
        body.put("message", fieldError == null ? "Invalid request" : fieldError.getField() + " is invalid");
        return body;
    }
}
