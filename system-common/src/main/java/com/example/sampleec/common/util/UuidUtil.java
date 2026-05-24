package com.example.sampleec.common.util;

import java.util.UUID;

/**
 * UUID 生成ユーティリティ。
 * 将来的に UUID v7 等への差し替えを容易にするためラッパーとして提供。
 */
public final class UuidUtil {

    private UuidUtil() {
    }

    /**
     * ランダムな UUID を文字列で返す。
     */
    public static String generate() {
        return UUID.randomUUID().toString();
    }
}
