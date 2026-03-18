package com.mavis.admin.domains.product.controller;

import com.mavis.admin.domains.product.dto.*;
import com.mavis.admin.domains.product.service.AdminProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/api/product")
@Tag(name = "관리자 상품 API")
public class AdminProductController {

    private final AdminProductService adminProductService;

    @GetMapping("/{id}")
    @Operation(summary = "상품 단건 조회")
    public GetProductResponse getProductById(@PathVariable Long id) {
        return adminProductService.getProductById(id);
    }

    @GetMapping
    @Operation(summary = "상품 목록 조회")
    public List<GetProductPreviewResponse> getProducts(Pageable pageable) {
        return adminProductService.getProductList(pageable);
    }

    @PostMapping
    @Operation(summary = "상품 등록")
    public CreateProductResponse createProduct(@RequestPart CreateProductRequest request,
                                               @RequestPart List<MultipartFile> mainImages,
                                               @RequestPart List<MultipartFile> productImages,
                                               @RequestPart List<MultipartFile> detailImages) {
        return adminProductService.createProduct(request, mainImages, productImages, detailImages);
    }

    @PutMapping("/{id}")
    @Operation(summary = "상품 수정")
    public void updateProduct(@PathVariable Long id,
                              @RequestPart UpdateProductRequest request,
                              @RequestPart(required = false) List<MultipartFile> mainImages,
                              @RequestPart(required = false) List<MultipartFile> productImages,
                              @RequestPart(required = false) List<MultipartFile> detailImages) {
        adminProductService.updateProduct(id, request, mainImages, productImages, detailImages);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "상품 삭제")
    public void deleteProduct(@PathVariable Long id) {
        adminProductService.deleteProduct(id);
    }

    @PatchMapping("/{id}/clearance")
    @Operation(summary = "상품 클리어런스 여부 수정")
    public void updateProductClearance(@PathVariable Long id, @RequestBody UpdateProductClearanceRequest request) {
        adminProductService.updateProductClearance(id, request);
    }
}
