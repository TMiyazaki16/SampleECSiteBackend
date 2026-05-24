package com.example.sampleec.presentation.response;

import com.example.sampleec.catalog.service.ProductListResult;

import java.util.List;

/**
 * 商品一覧レスポンスデータ。
 * フロントエンドが期待する { "data": { "items": [...], "total": N, "page": N, "perPage": N } } 形式。
 */
public record ProductListResponseData(
        List<ProductData> items,
        long total,
        int page,
        int perPage
) {
    public static ProductListResponseData from(ProductListResult result) {
        List<ProductData> items = result.items().stream()
                .map(ProductData::from)
                .toList();
        return new ProductListResponseData(items, result.total(), result.page(), result.perPage());
    }
}
