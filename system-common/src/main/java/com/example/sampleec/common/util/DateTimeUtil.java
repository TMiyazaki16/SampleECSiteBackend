package com.example.sampleec.common.util;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * 日時ユーティリティ。
 */
public final class DateTimeUtil {

    private DateTimeUtil() {
    }

    /**
     * 現在の UTC 日時を返す。
     */
    public static LocalDateTime nowUtc() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }
}
