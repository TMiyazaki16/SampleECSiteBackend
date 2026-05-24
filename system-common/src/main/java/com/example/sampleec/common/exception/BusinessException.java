package com.example.sampleec.common.exception;

import org.springframework.http.HttpStatus;

/**
 * 業務例外
 * 業務フロー上、想定されるエラーを表す。
 */
public class BusinessException extends RuntimeException {

    private final HttpStatus status;

    public BusinessException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public BusinessException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
