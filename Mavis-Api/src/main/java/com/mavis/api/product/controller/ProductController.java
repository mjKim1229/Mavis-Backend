package com.mavis.api.product.controller;

import com.mavis.api.product.dto.GetProductPreviewResponse;
import com.mavis.api.product.dto.GetProductResponse;
import com.mavis.api.product.dto.SubCategoryVO;
import com.mavis.api.product.service.ProductService;
import com.mavis.common.enums.ProductCategory;
import com.mavis.common.enums.ProductSubCategory;
import com.mavis.domain.domains.product.vo.ProductNoticeResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/api/products")
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "단일 상품 조회", description = "ID를 이용하여 단일 상품을 조회합니다.")
    @GetMapping("/{id}")
    public GetProductResponse getProductResponse(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    @Operation(summary = "상품 카테고리 조회", description = "상품 카테고리 및 하위 카테고리 목록을 조회합니다.")
    @GetMapping("/category")
    public List<SubCategoryVO> getProductCategory() {
        return productService.getProductTotalCategory();
    }

    @Operation(summary = "상품 공지 (notice) 조회")
    @GetMapping("/{id}/notice")
    public ProductNoticeResponse getProductNotice(@PathVariable Long id) {
        return productService.getProductNotice(id);
    }

    @Operation(summary = "주간 인기 상품 조회", description = "주간 인기 상품 목록을 조회합니다.")
    @GetMapping("/popular")
    public List<GetProductPreviewResponse> getWeeklyPopularProduct(Pageable pageable) {
        return productService.getThisWeekPopularProducts(pageable);
    }

    @Operation(summary = "최근 등록 상품 조회", description = "최근 등록된 상품 목록을 조회합니다.")
    @GetMapping("/recent")
    public List<GetProductPreviewResponse> getRecentCreatedProduct(Pageable pageable) {
        return productService.getRecentCreatedProducts(pageable);
    }

    @Operation(summary = "클리어런스 상품 조회", description = "클리어런스 상품 목록을 조회합니다.")
    @GetMapping("/clearance")
    public List<GetProductPreviewResponse> getClearanceProduct(Pageable pageable) {
        return productService.getClearanceProduct(pageable);
    }

    @Operation(summary = "카테고리 및 하위 카테고리로 상품 미리보기 조회")
    @GetMapping("/category/products")
    public List<GetProductPreviewResponse> getProductPreviewsByCategory(
            @RequestParam ProductCategory productCategory,
            @RequestParam(required = false) ProductSubCategory subCategory,
            Pageable pageable) {
        return productService.getProductPreviewResponseBySubCategory(productCategory, subCategory, pageable);
    }
}
