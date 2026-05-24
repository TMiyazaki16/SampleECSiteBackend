package com.example.sampleec.infrastructure.repository;

import com.example.sampleec.catalog.entity.Product;
import com.example.sampleec.infrastructure.entity.OrderItemTableEntity;
import com.example.sampleec.infrastructure.entity.OrderTableEntity;
import com.example.sampleec.infrastructure.mapper.OrderItemMapper;
import com.example.sampleec.infrastructure.mapper.OrderMapper;
import com.example.sampleec.infrastructure.mapper.ProductMapper;
import com.example.sampleec.order.entity.Order;
import com.example.sampleec.order.entity.OrderItem;
import com.example.sampleec.order.repository.OrderRepository;
import com.example.sampleec.order.entity.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * OrderRepository の MyBatis 実装。
 * N+1 を避けるため商品情報は IN 句で一括取得。
 */
@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;
    private final ProductRepositoryImpl productRepository;

    @Override
    public List<Order> findByBuyerId(String buyerId) {
        List<OrderTableEntity> orderEntities = orderMapper.findByBuyerId(buyerId);
        if (orderEntities.isEmpty()) {
            return List.of();
        }

        // 注文IDリストで明細を一括取得（N+1 回避）
        List<String> orderIds = orderEntities.stream()
                .map(OrderTableEntity::getId)
                .toList();
        List<OrderItemTableEntity> itemEntities = orderItemMapper.findByOrderIds(orderIds);

        // 商品IDリストで商品情報を一括取得（N+1 回避）
        List<String> productIds = itemEntities.stream()
                .map(OrderItemTableEntity::getProductId)
                .distinct()
                .toList();
        Map<String, Product> productMap = productRepository.findByIds(productIds).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        // orderId → items のグループ化
        Map<String, List<OrderItem>> itemsByOrderId = itemEntities.stream()
                .collect(Collectors.groupingBy(
                        OrderItemTableEntity::getOrderId,
                        Collectors.mapping(
                                item -> OrderItem.builder()
                                        .id(item.getId())
                                        .product(productMap.get(item.getProductId()))
                                        .quantity(item.getQuantity())
                                        .unitPrice(item.getUnitPrice())
                                        .build(),
                                Collectors.toList()
                        )
                ));

        return orderEntities.stream()
                .map(o -> Order.builder()
                        .id(o.getId())
                        .buyerId(o.getBuyerId())
                        .totalPrice(o.getTotalPrice())
                        .status(OrderStatus.valueOf(o.getStatus()))
                        .createdAt(o.getCreatedAt())
                        .items(itemsByOrderId.getOrDefault(o.getId(), List.of()))
                        .build())
                .toList();
    }

    @Override
    public void save(Order order) {
        // 注文ヘッダー保存
        OrderTableEntity orderEntity = new OrderTableEntity();
        orderEntity.setId(order.getId());
        orderEntity.setBuyerId(order.getBuyerId());
        orderEntity.setTotalPrice(order.getTotalPrice());
        orderEntity.setStatus(order.getStatus().name());
        orderEntity.setCreatedAt(order.getCreatedAt());
        orderEntity.setUpdatedAt(order.getCreatedAt());
        orderMapper.insert(orderEntity);

        // 注文明細保存
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            List<OrderItemTableEntity> itemEntities = order.getItems().stream()
                    .map(item -> {
                        OrderItemTableEntity e = new OrderItemTableEntity();
                        e.setId(item.getId());
                        e.setOrderId(order.getId());
                        e.setProductId(item.getProduct().getId());
                        e.setQuantity(item.getQuantity());
                        e.setUnitPrice(item.getUnitPrice());
                        return e;
                    })
                    .toList();
            orderItemMapper.insertAll(itemEntities);
        }
    }
}
