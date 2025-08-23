package com.mavis.api.product.controller;

import com.mavis.api.product.dto.GetProductResponse;
import com.mavis.api.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/api/products")
public class ProductController {

    private final ProductService productService;

    @GetMapping("/{id}")
    public GetProductResponse getProductResponse(@PathVariable Long id) {
        return productService.getProductById(id);
    }
}
