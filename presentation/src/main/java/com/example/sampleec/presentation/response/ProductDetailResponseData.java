package com.example.sampleec.presentation.response;

/**
 * 商品詳細レスポンスデータ。
 * フロントエンドが期待する { "data": { "item": {...} } } 形式。
 */
public record ProductDetailResponseData(ProductData item) {
}
