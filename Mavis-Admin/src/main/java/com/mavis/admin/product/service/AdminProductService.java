package com.mavis.admin.product.service;

import com.mavis.admin.product.dto.CreateProductRequest;
import com.mavis.admin.product.implement.ProductColorAppender;
import com.mavis.domains.product.domain.Product;
import com.mavis.domains.product.domain.ProductNotice;
import com.mavis.domains.product.repository.ProductNoticeRepository;
import com.mavis.domains.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminProductService {

    private final ProductColorAppender productColorAppender;
    private final ProductRepository productRepository;
    private final ProductNoticeRepository productNoticeRepository;

    @Transactional
    public void createProduct(CreateProductRequest request) {
        Product product = request.toProduct();
        Product savedProduct = productRepository.save(product);

        ProductNotice productNotice = request.toProductNotice(savedProduct);
        productNoticeRepository.save(productNotice);

        productColorAppender.saveProductColors(request.colors(), savedProduct);
    }
}
