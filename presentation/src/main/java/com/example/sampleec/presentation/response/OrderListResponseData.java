package com.example.sampleec.presentation.response;

import com.example.sampleec.order.entity.Order;

import java.util.List;

/**
 * 注文一覧レスポンスデータ。
 * フロントエンドが期待する { "data": { "items": [...] } } 形式。
 */
public record OrderListResponseData(List<OrderData> items) {

    public static OrderListResponseData from(List<Order> orders) {
        List<OrderData> items = orders.stream()
                .map(OrderData::from)
                .toList();
        return new OrderListResponseData(items);
    }
}
