package com.example.sampleec.order.entity;

import com.example.sampleec.catalog.entity.Product;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

/**
 * 注文明細。
 * 注文確定時点の unitPrice を固定する（価格改定の影響を受けない）。
 */
@Value
@Builder
public class OrderItem {

    /** 注文明細ID */
    String id;
    /** 商品情報（フロントエンドの OrderItem.product に対応） */
    Product product;
    int quantity;
    /** 注文確定時点の単価 */
    BigDecimal unitPrice;
}
