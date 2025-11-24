package com.mavis.api.product.controller;

import com.mavis.api.product.dto.GetProductPreviewResponse;
import com.mavis.api.product.dto.GetProductResponse;
import com.mavis.common.enums.ProductCategory;
import com.mavis.common.enums.ProductSubCategory;
import com.mavis.domain.domains.product.vo.ProductNoticeResponse;
import com.mavis.api.product.dto.SubCategoryVO;
import com.mavis.api.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/api/products")
public class ProductController {

    private final ProductService productService;

    @GetMapping("/{id}")
    public GetProductResponse getProductResponse(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    @GetMapping("/category")
    public List<SubCategoryVO> getProductCategory() {
        return productService.getProductTotalCategory();
    }

    @GetMapping("/{id}/notice")
    public ProductNoticeResponse getProductNotice(@PathVariable Long id) {
        return productService.getProductNotice(id);
    }

    @GetMapping("/popular")
    public List<GetProductPreviewResponse> getWeeklyPopularProduct(Pageable pageable) {
        return productService.getThisWeekPopularProducts(pageable);
    }

    @GetMapping("/recent")
    public List<GetProductPreviewResponse> getRecentCreatedProduct(Pageable pageable) {
        return productService.getRecentCreatedProducts(pageable);
    }

    @GetMapping("/clearance")
    public List<GetProductPreviewResponse> getClearanceProduct(Pageable pageable) {
        return productService.getClearanceProduct(pageable);
    }

    @GetMapping("/category/products")
    public List<GetProductPreviewResponse> getProductPreviewsByCategory(
            @RequestParam ProductCategory productCategory,
            @RequestParam(required = false) ProductSubCategory subCategory,
            Pageable pageable) {
        return productService.getProductPreviewResponseBySubCategory(productCategory, subCategory, pageable);
    }
}
