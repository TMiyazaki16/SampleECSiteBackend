package com.example.sampleec.presentation.response;

import com.example.sampleec.order.entity.Order;

/**
 * 注文作成レスポンスデータ。
 * フロントエンドが期待する { "data": { "order": {...} } } 形式。
 */
public record OrderResponseData(OrderData order) {

    public static OrderResponseData from(Order order) {
        return new OrderResponseData(OrderData.from(order));
    }
}
