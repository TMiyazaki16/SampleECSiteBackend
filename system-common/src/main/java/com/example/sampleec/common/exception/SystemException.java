package com.example.sampleec.common.exception;

/**
 * システム例外
 * 予期しない内部エラー（DB障害、NullPointerException 等）を表す。
 * HTTP 500 に対応する。
 */
public class SystemException extends RuntimeException {

    public SystemException(String message, Throwable cause) {
        super(message, cause);
    }

    public SystemException(String message) {
        super(message);
    }
}
