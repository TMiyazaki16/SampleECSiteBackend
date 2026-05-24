package com.example.sampleec.infrastructure.mapper;

import com.example.sampleec.infrastructure.entity.OrderItemTableEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * order_items テーブルの MyBatis Mapper。
 */
@Mapper
public interface OrderItemMapper {

    List<OrderItemTableEntity> findByOrderId(@Param("orderId") String orderId);

    /**
     * 複数注文の明細を一括取得（N+1 回避）。
     */
    List<OrderItemTableEntity> findByOrderIds(@Param("orderIds") List<String> orderIds);

    void insertAll(@Param("items") List<OrderItemTableEntity> items);
}
