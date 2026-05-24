package com.example.sampleec.order.entity;

/**
 * 注文ステータス。
 * pending → confirmed → shipped → delivered
 */
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    SHIPPED,
    DELIVERED;

    /**
     * フロントエンドの型定義（'pending' | 'confirmed' | 'shipped' | 'delivered'）に合わせた小文字表現を返す。
     */
    public String toLowerCaseValue() {
        return this.name().toLowerCase();
    }
}
