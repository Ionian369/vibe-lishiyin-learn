package com.school.reservation.exception;

import lombok.Getter;

/**
 * 业务异常：code 同时作为 HTTP 状态码返回（如 409 冲突），message 直接透传给前端展示
 */
@Getter
public class BizException extends RuntimeException {

    private final int code;

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }
}
