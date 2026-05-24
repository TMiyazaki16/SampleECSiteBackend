package com.example.sampleec.presentation.response;

/**
 * ログインレスポンスデータ。
 * フロントエンドが期待する { "data": { "user": {...}, "token": "..." } } 形式。
 */
public record AuthResponseData(UserData user, String token) {
}
