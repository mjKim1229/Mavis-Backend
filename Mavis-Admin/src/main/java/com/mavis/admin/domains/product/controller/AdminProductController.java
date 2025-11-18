package com.mavis.admin.domains.product.controller;

import com.mavis.admin.domains.product.dto.CreateProductRequest;
import com.mavis.admin.domains.product.dto.CreateProductResponse;
import com.mavis.admin.domains.product.dto.GetProductResponse;
import com.mavis.admin.domains.product.service.AdminProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/api/product")
public class AdminProductController {

    private final AdminProductService adminProductService;

    @GetMapping
    public  List<GetProductResponse> getProducts(Pageable pageable) {
        return adminProductService.getProductList(pageable);
    }

    @PostMapping
    public CreateProductResponse createProduct(@RequestPart CreateProductRequest request,
                                               @RequestPart List<MultipartFile> mainImages,
                                               @RequestPart List<MultipartFile> productImages,
                                               @RequestPart List<MultipartFile> detailImages) {
        return adminProductService.createProduct(request, mainImages, productImages, detailImages);
    }

    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id) {
        adminProductService.deleteProduct(id);
    }
}
