package com.school.reservation.exception;

import com.school.reservation.dto.Result;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理：统一转成 Result 结构，前端只消费 message 字段
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 业务异常（含 409 冲突），原因文案直接透传给前端展示 */
    @ExceptionHandler(BizException.class)
    public ResponseEntity<Result<Void>> handleBiz(BizException e) {
        return ResponseEntity.status(e.getCode()).body(Result.fail(e.getCode(), e.getMessage()));
    }

    /** 参数校验失败：返回第一条校验错误提示 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fieldError -> fieldError.getDefaultMessage())
                .orElse("参数不合法");
        return ResponseEntity.badRequest().body(Result.fail(400, message));
    }
}
