package com.example.sampleec.presentation.response;

import com.example.sampleec.order.entity.Order;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 注文レスポンス DTO。
 * フロントエンドの Order 型と完全に対応。
 * createdAt は ISO 8601 形式（UTC）で返す。
 */
public record OrderData(
        String id,
        List<OrderItemData> items,
        BigDecimal totalPrice,
        String status,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        LocalDateTime createdAt,
        String buyerId
) {
    public static OrderData from(Order order) {
        List<OrderItemData> items = order.getItems().stream()
                .map(OrderItemData::from)
                .toList();
        return new OrderData(
                order.getId(),
                items,
                order.getTotalPrice(),
                order.getStatus().toLowerCaseValue(),
                order.getCreatedAt(),
                order.getBuyerId()
        );
    }
}
