package com.example.sampleec.presentation.response;

import com.example.sampleec.order.entity.OrderItem;

import java.math.BigDecimal;

/**
 * 注文明細レスポンス DTO。
 * フロントエンドの OrderItem 型と完全に対応。
 */
public record OrderItemData(
        ProductData product,
        int quantity,
        BigDecimal unitPrice
) {
    public static OrderItemData from(OrderItem item) {
        return new OrderItemData(
                ProductData.from(item.getProduct()),
                item.getQuantity(),
                item.getUnitPrice()
        );
    }
}
