package com.example.sampleec.presentation.response;

/**
 * ユーザー情報取得レスポンスデータ。
 * フロントエンドが期待する { "data": { "user": {...} } } 形式。
 */
public record UserResponseData(UserData user) {
}
