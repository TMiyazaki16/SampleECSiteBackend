package com.example.sampleec.presentation.controller;

import com.example.sampleec.catalog.entity.Product;
import com.example.sampleec.catalog.service.ProductApplicationService;
import com.example.sampleec.catalog.service.ProductListResult;
import com.example.sampleec.presentation.response.ApiResponse;
import com.example.sampleec.presentation.response.ProductData;
import com.example.sampleec.presentation.response.ProductDetailResponseData;
import com.example.sampleec.presentation.response.ProductListResponseData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商品カタログコントローラー。
 */
@Tag(name = "Products", description = "商品 API")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductApplicationService productApplicationService;

    /**
     * 商品一覧取得（ページネーション・カテゴリフィルタ対応）。
     */
    @Operation(summary = "商品一覧取得", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping
    public ResponseEntity<ApiResponse<ProductListResponseData>> getProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "6") int perPage,
            @RequestParam(required = false) String category) {

        ProductListResult result = productApplicationService.getProducts(page, perPage, category);
        return ResponseEntity.ok(ApiResponse.of(ProductListResponseData.from(result)));
    }

    /**
     * 商品詳細取得。
     */
    @Operation(summary = "商品詳細取得", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDetailResponseData>> getProduct(
            @PathVariable String id) {

        Product product = productApplicationService.getProduct(id);
        return ResponseEntity.ok(ApiResponse.of(new ProductDetailResponseData(ProductData.from(product))));
    }
}
