package com.example.sampleec.authentication.entity;

/**
 * ユーザーロール。
 */
public enum Role {
    ADMIN,
    BUYER;

    /**
     * フロントエンドの型定義（'admin' | 'buyer'）に合わせた小文字表現を返す。
     */
    public String toLowerCaseValue() {
        return this.name().toLowerCase();
    }
}
