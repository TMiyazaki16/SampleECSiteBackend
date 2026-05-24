package com.example.sampleec.presentation.response;

/**
 * エラーレスポンス。
 * フロントエンドが期待する { "message": "..." } 形式。
 */
public record ErrorResponse(String message) {
}
