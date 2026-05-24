package com.example.sampleec.order.service.domain;

import com.example.sampleec.catalog.entity.Product;
import com.example.sampleec.catalog.repository.ProductRepository;
import com.example.sampleec.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * 在庫ドメインサービス。
 * 在庫チェックと楽観ロック付き在庫更新を担う。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StockDomainService {

    private final ProductRepository productRepository;

    /**
     * 在庫を確認し、問題なければ楽観ロックで在庫を減らす。
     *
     * @param product  商品
     * @param quantity 購入数量
     * @throws BusinessException 在庫不足（400）またはロック競合（409）
     */
    public void validateAndDeductStock(Product product, int quantity) {
        if (product.getStock() < quantity) {
            log.warn("在庫不足: productId={}, 在庫={}, 要求数量={}",
                    product.getId(), product.getStock(), quantity);
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "商品「" + product.getName() + "」の在庫が不足しています");
        }

        int updated = productRepository.updateStockWithLock(
                product.getId(), quantity, product.getVersion());

        if (updated == 0) {
            log.warn("楽観ロック競合または在庫不足: productId={}", product.getId());
            throw new BusinessException(HttpStatus.CONFLICT,
                    "在庫の更新に失敗しました。再度お試しください");
        }

        log.info("在庫更新成功: productId={}, quantity={}", product.getId(), quantity);
    }
}
