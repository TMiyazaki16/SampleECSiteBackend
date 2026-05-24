package com.example.sampleec.presentation.response;

/**
 * API 共通レスポンスラッパー。
 * フロントエンドが期待する { "data": T } 形式。
 */
public record ApiResponse<T>(T data) {

    public static <T> ApiResponse<T> of(T data) {
        return new ApiResponse<>(data);
    }
}
