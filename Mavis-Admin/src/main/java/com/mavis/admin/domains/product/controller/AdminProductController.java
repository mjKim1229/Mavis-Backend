package com.mavis.admin.domains.product.controller;

import com.mavis.admin.domains.product.dto.CreateProductRequest;
import com.mavis.admin.domains.product.dto.CreateProductResponse;
import com.mavis.admin.domains.product.service.AdminProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/api/product")
public class AdminProductController {

    private final AdminProductService adminProductService;

    @PostMapping
    public CreateProductResponse createProduct(@RequestPart CreateProductRequest request,
                                               @RequestPart List<MultipartFile> mainImages,
                                               @RequestPart List<MultipartFile> detailImages) {
        return adminProductService.createProduct(request, mainImages, detailImages);
    }
}
