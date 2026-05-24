package com.example.sampleec.presentation.response;

import com.example.sampleec.catalog.entity.Product;

import java.math.BigDecimal;

/**
 * 商品レスポンス DTO。
 * フロントエンドの Product 型と完全に対応。
 */
public record ProductData(
        String id,
        String name,
        String description,
        BigDecimal price,
        String currency,
        int stock,
        String category,
        String imageUrl
) {
    public static ProductData from(Product product) {
        return new ProductData(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCurrency().name(),
                product.getStock(),
                product.getCategory(),
                product.getImageUrl()
        );
    }
}
