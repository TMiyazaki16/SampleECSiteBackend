package com.example.sampleec.infrastructure.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * order_items テーブルのマッピング POJO。
 */
@Data
public class OrderItemTableEntity {
    private String id;
    private String orderId;
    private String productId;
    private int quantity;
    private BigDecimal unitPrice;
}
