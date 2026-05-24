package com.example.sampleec.catalog.entity;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品エンティティ。
 * 楽観ロック用の version フィールドを持つ。
 */
@Value
@Builder
public class Product {

    String id;
    String name;
    String description;
    /** 価格（金額計算に double は不使用）。 */
    BigDecimal price;
    Currency currency;
    int stock;
    String category;
    String imageUrl;
    /** 楽観ロック用バージョン番号。 */
    int version;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
