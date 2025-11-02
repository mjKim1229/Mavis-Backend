package com.mavis.admin.domains.product.service;

import com.mavis.admin.domains.product.dto.CreateProductRequest;
import com.mavis.admin.domains.product.dto.CreateProductResponse;
import com.mavis.admin.domains.product.implement.ProductColorAppender;
import com.mavis.admin.domains.product.implement.ProductImageAppender;
import com.mavis.domain.domains.product.domain.Product;
import com.mavis.domain.domains.product.domain.ProductNotice;
import com.mavis.domain.domains.product.implement.ProductReader;
import com.mavis.domain.domains.product.repository.ProductNoticeRepository;
import com.mavis.domain.domains.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminProductService {

    private final ProductColorAppender productColorAppender;
    private final ProductRepository productRepository;
    private final ProductNoticeRepository productNoticeRepository;
    private final ProductImageAppender productImageAppender;
    private final ProductReader productReader;

    @Transactional
    public CreateProductResponse createProduct(CreateProductRequest request, List<MultipartFile> mainImages, List<MultipartFile> detailImages) {
        Product product = request.toProduct();
        Product savedProduct = productRepository.save(product);

        ProductNotice productNotice = request.toProductNotice(savedProduct);
        productNoticeRepository.save(productNotice);

        productColorAppender.saveProductColors(request.colors(), savedProduct);
        productImageAppender.saveImages(mainImages, detailImages, savedProduct);
        return new CreateProductResponse(product.getId());
    }

    @Transactional
    public void deleteProduct(Long productId) {
        Product product = productReader.readById(productId);
        product.delete();
    }
}
