package com.example.sampleec.order.repository;

import com.example.sampleec.order.entity.Order;

import java.util.List;

/**
 * 注文リポジトリインターフェース。
 */
public interface OrderRepository {

    List<Order> findByBuyerId(String buyerId);

    void save(Order order);
}
