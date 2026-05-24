package com.example.sampleec.presentation.controller;

import com.example.sampleec.order.entity.Order;
import com.example.sampleec.order.service.OrderApplicationService;
import com.example.sampleec.presentation.request.CreateOrderRequest;
import com.example.sampleec.presentation.response.ApiResponse;
import com.example.sampleec.presentation.response.OrderListResponseData;
import com.example.sampleec.presentation.response.OrderResponseData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 注文コントローラー。
 */
@Tag(name = "Orders", description = "注文 API")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderApplicationService orderApplicationService;

    /**
     * 注文履歴取得（ログインユーザーの注文のみ）。
     */
    @Operation(summary = "注文履歴取得", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping
    public ResponseEntity<ApiResponse<OrderListResponseData>> getOrders(Authentication authentication) {
        String buyerId = authentication.getName();
        List<Order> orders = orderApplicationService.getOrders(buyerId);
        return ResponseEntity.ok(ApiResponse.of(OrderListResponseData.from(orders)));
    }

    /**
     * 注文作成。
     * buyerId は JWT から取得（改ざん防止）。
     * totalPrice はサーバー側で計算（改ざん防止）。
     */
    @Operation(summary = "注文作成", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ApiResponse<OrderResponseData>> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            Authentication authentication) {

        String buyerId = authentication.getName();

        // フロントエンドのリクエスト形式をアプリケーションサービスの型に変換
        List<OrderApplicationService.OrderItemRequest> itemRequests = request.items().stream()
                .map(item -> new OrderApplicationService.OrderItemRequest(
                        item.productId(), item.quantity()))
                .toList();

        Order order = orderApplicationService.createOrder(buyerId, itemRequests);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.of(OrderResponseData.from(order)));
    }
}
