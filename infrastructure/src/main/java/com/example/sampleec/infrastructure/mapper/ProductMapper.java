package com.example.sampleec.infrastructure.mapper;

import com.example.sampleec.infrastructure.entity.ProductTableEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * products テーブルの MyBatis Mapper。
 */
@Mapper
public interface ProductMapper {

    List<ProductTableEntity> findAll(@Param("offset") int offset, @Param("limit") int limit);

    List<ProductTableEntity> findByCategory(@Param("category") String category,
                                            @Param("offset") int offset,
                                            @Param("limit") int limit);

    long countAll();

    long countByCategory(@Param("category") String category);

    ProductTableEntity findById(@Param("id") String id);

    List<ProductTableEntity> findByIds(@Param("ids") List<String> ids);

    /**
     * 楽観ロック付き在庫更新。
     *
     * @return 更新行数（0 = 在庫不足またはバージョン不一致）
     */
    int updateStockWithLock(@Param("id") String id,
                            @Param("quantity") int quantity,
                            @Param("version") int version);

    void insert(ProductTableEntity entity);
}
