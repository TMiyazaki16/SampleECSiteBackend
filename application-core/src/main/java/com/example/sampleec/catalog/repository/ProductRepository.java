package com.example.sampleec.catalog.repository;

import com.example.sampleec.catalog.entity.Product;

import java.util.List;
import java.util.Optional;

/**
 * 商品リポジトリインターフェース。
 */
public interface ProductRepository {

    List<Product> findAll(int offset, int limit);

    List<Product> findByCategory(String category, int offset, int limit);

    long countAll();

    long countByCategory(String category);

    Optional<Product> findById(String id);

    /**
     * 楽観ロック付きで在庫を減らす。
     *
     * @param id       商品ID
     * @param quantity 減らす数量
     * @param version  楽観ロック用バージョン
     * @return 更新行数（0 の場合は在庫不足またはロック競合）
     */
    int updateStockWithLock(String id, int quantity, int version);

    void save(Product product);
}
