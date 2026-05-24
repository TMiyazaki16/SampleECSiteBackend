package com.example.sampleec.presentation.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * 注文作成リクエスト。
 */
public record CreateOrderRequest(
        @NotEmpty(message = "注文アイテムは1件以上必要です")
        @Valid
        List<OrderItemRequest> items
) {
    /**
     * 注文明細リクエスト。
     */
    public record OrderItemRequest(
            @NotBlank(message = "商品IDは必須です")
            String productId,

            @Min(value = 1, message = "数量は1以上である必要があります")
            int quantity
    ) {
    }
}
