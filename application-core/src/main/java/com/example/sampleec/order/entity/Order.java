package com.example.sampleec.order.entity;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 注文エンティティ。
 * 注文集約のルートオブジェクト。
 */
@Value
@Builder
public class Order {

    String id;
    /** 注文明細リスト */
    List<OrderItem> items;
    /** 合計金額（サーバー側で計算） */
    BigDecimal totalPrice;
    OrderStatus status;
    LocalDateTime createdAt;
    /** 購入者ユーザーID（JWTから取得） */
    String buyerId;
}
