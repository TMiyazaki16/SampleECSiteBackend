package com.example.sampleec.infrastructure.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * orders テーブルのマッピング POJO。
 */
@Data
public class OrderTableEntity {
    private String id;
    private String buyerId;
    private BigDecimal totalPrice;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
