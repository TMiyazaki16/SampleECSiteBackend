package com.example.sampleec.infrastructure.mapper;

import com.example.sampleec.infrastructure.entity.OrderTableEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * orders テーブルの MyBatis Mapper。
 */
@Mapper
public interface OrderMapper {

    List<OrderTableEntity> findByBuyerId(@Param("buyerId") String buyerId);

    void insert(OrderTableEntity entity);
}
