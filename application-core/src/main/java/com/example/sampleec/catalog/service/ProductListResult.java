package com.example.sampleec.catalog.service;

import com.example.sampleec.catalog.entity.Product;

import java.util.List;

/**
 * 商品一覧取得結果。
 * ページネーション情報を含む。
 */
public record ProductListResult(
        List<Product> items,
        long total,
        int page,
        int perPage
) {
}
