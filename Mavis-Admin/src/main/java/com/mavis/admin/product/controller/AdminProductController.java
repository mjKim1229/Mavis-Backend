package com.mavis.admin.product.controller;

import com.mavis.admin.product.dto.CreateProductRequest;
import com.mavis.admin.product.service.AdminProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/api/product")
public class AdminProductController {

    private final AdminProductService adminProductService;

    @PostMapping
    public void createProduct(CreateProductRequest request) {
        adminProductService.createProduct(request);
    }
}
