package com.example.sampleec.order.service;

import com.example.sampleec.catalog.entity.Product;
import com.example.sampleec.catalog.repository.ProductRepository;
import com.example.sampleec.common.exception.BusinessException;
import com.example.sampleec.common.util.DateTimeUtil;
import com.example.sampleec.common.util.UuidUtil;
import com.example.sampleec.order.entity.Order;
import com.example.sampleec.order.entity.OrderItem;
import com.example.sampleec.order.entity.OrderStatus;
import com.example.sampleec.order.repository.OrderRepository;
import com.example.sampleec.order.service.domain.StockDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 注文アプリケーションサービス。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderApplicationService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final StockDomainService stockDomainService;

    /**
     * 注文履歴を取得する。
     *
     * @param buyerId 購入者ユーザーID
     * @return 注文リスト
     */
    @Transactional(readOnly = true)
    public List<Order> getOrders(String buyerId) {
        return orderRepository.findByBuyerId(buyerId);
    }

    /**
     * 注文を作成する。
     * - 商品の存在確認
     * - 在庫チェック・楽観ロック更新
     * - totalPrice をサーバー側で計算
     * - 注文を保存
     *
     * @param buyerId 購入者ユーザーID（JWTから取得済み）
     * @param items   注文明細リスト（productId + quantity）
     * @return 作成された注文
     */
    @Transactional
    public Order createOrder(String buyerId, List<OrderItemRequest> items) {
        log.info("注文作成開始: buyerId={}, アイテム数={}", buyerId, items.size());

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;
        LocalDateTime now = DateTimeUtil.nowUtc();

        for (OrderItemRequest itemRequest : items) {
            // 商品取得（存在しなければ 404）
            Product product = productRepository.findById(itemRequest.productId())
                    .orElseThrow(() -> {
                        log.warn("注文対象商品が見つかりません: productId={}", itemRequest.productId());
                        return new BusinessException(HttpStatus.NOT_FOUND,
                                "商品が見つかりません: " + itemRequest.productId());
                    });

            // 在庫確認・楽観ロック更新
            stockDomainService.validateAndDeductStock(product, itemRequest.quantity());

            // 注文明細を構築（注文確定時の単価を固定）
            OrderItem orderItem = OrderItem.builder()
                    .id(UuidUtil.generate())
                    .product(product)
                    .quantity(itemRequest.quantity())
                    .unitPrice(product.getPrice())
                    .build();
            orderItems.add(orderItem);

            // 合計金額計算
            totalPrice = totalPrice.add(
                    product.getPrice().multiply(BigDecimal.valueOf(itemRequest.quantity())));
        }

        // 注文エンティティ生成
        Order order = Order.builder()
                .id(UuidUtil.generate())
                .buyerId(buyerId)
                .items(orderItems)
                .totalPrice(totalPrice)
                .status(OrderStatus.PENDING)
                .createdAt(now)
                .build();

        orderRepository.save(order);
        log.info("注文作成完了: orderId={}", order.getId());
        return order;
    }

    /**
     * 注文明細リクエスト（内部用）。
     */
    public record OrderItemRequest(String productId, int quantity) {
    }
}
