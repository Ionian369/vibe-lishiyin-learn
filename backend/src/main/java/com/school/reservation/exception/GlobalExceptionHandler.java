package com.school.reservation.exception;

import com.school.reservation.dto.Result;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
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

    /** 缺少必填 query 参数（如 GET /api/reservations 不带 user）：同样落 Result 结构的 400 */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Result<Void>> handleMissingParam(MissingServletRequestParameterException e) {
        return ResponseEntity.badRequest().body(Result.fail(400, "缺少必填参数：" + e.getParameterName()));
    }
}
