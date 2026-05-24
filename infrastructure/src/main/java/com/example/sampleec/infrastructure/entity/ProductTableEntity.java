package com.example.sampleec.infrastructure.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * products テーブルのマッピング POJO。
 */
@Data
public class ProductTableEntity {
    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private String currency;
    private int stock;
    private String category;
    private String imageUrl;
    private int version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
