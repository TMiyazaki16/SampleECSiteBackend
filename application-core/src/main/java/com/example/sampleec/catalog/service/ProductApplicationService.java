package com.example.sampleec.catalog.service;

import com.example.sampleec.catalog.entity.Product;
import com.example.sampleec.catalog.repository.ProductRepository;
import com.example.sampleec.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商品カタログアプリケーションサービス。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductApplicationService {

    private final ProductRepository productRepository;

    /**
     * 商品一覧を取得する（ページネーション・カテゴリフィルタ対応）。
     *
     * @param page     ページ番号（1始まり）
     * @param perPage  1ページあたりの件数
     * @param category カテゴリ（null の場合は全件）
     * @return 商品一覧と総件数
     */
    public ProductListResult getProducts(int page, int perPage, String category) {
        int offset = (page - 1) * perPage;

        List<Product> items;
        long total;

        if (category != null && !category.isBlank()) {
            items = productRepository.findByCategory(category, offset, perPage);
            total = productRepository.countByCategory(category);
        } else {
            items = productRepository.findAll(offset, perPage);
            total = productRepository.countAll();
        }

        return new ProductListResult(items, total, page, perPage);
    }

    /**
     * 商品詳細を取得する。
     *
     * @param id 商品ID
     * @return 商品
     * @throws BusinessException 商品が存在しない場合（404）
     */
    public Product getProduct(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("商品が見つかりません: id={}", id);
                    return new BusinessException(HttpStatus.NOT_FOUND, "商品が見つかりません");
                });
    }
}
