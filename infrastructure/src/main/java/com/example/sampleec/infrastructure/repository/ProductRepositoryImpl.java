package com.example.sampleec.infrastructure.repository;

import com.example.sampleec.catalog.entity.Currency;
import com.example.sampleec.catalog.entity.Product;
import com.example.sampleec.catalog.repository.ProductRepository;
import com.example.sampleec.infrastructure.entity.ProductTableEntity;
import com.example.sampleec.infrastructure.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ProductRepository の MyBatis 実装。
 */
@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductMapper productMapper;

    @Override
    public List<Product> findAll(int offset, int limit) {
        return productMapper.findAll(offset, limit)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Product> findByCategory(String category, int offset, int limit) {
        return productMapper.findByCategory(category, offset, limit)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public long countAll() {
        return productMapper.countAll();
    }

    @Override
    public long countByCategory(String category) {
        return productMapper.countByCategory(category);
    }

    @Override
    public Optional<Product> findById(String id) {
        ProductTableEntity entity = productMapper.findById(id);
        return Optional.ofNullable(entity).map(this::toDomain);
    }

    @Override
    public int updateStockWithLock(String id, int quantity, int version) {
        return productMapper.updateStockWithLock(id, quantity, version);
    }

    @Override
    public void save(Product product) {
        productMapper.insert(toEntity(product));
    }

    /**
     * IDリストから商品マップを取得（N+1 回避用）。
     */
    public List<Product> findByIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return productMapper.findByIds(ids)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private Product toDomain(ProductTableEntity entity) {
        return Product.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .currency(Currency.valueOf(entity.getCurrency()))
                .stock(entity.getStock())
                .category(entity.getCategory())
                .imageUrl(entity.getImageUrl())
                .version(entity.getVersion())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private ProductTableEntity toEntity(Product product) {
        ProductTableEntity entity = new ProductTableEntity();
        entity.setId(product.getId());
        entity.setName(product.getName());
        entity.setDescription(product.getDescription());
        entity.setPrice(product.getPrice());
        entity.setCurrency(product.getCurrency().name());
        entity.setStock(product.getStock());
        entity.setCategory(product.getCategory());
        entity.setImageUrl(product.getImageUrl());
        entity.setVersion(product.getVersion());
        entity.setCreatedAt(product.getCreatedAt());
        entity.setUpdatedAt(product.getUpdatedAt());
        return entity;
    }
}
