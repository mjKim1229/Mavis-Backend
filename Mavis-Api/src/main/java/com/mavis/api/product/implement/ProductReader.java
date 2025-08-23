package com.mavis.api.product.implement;

import com.mavis.api.product.domain.Product;
import com.mavis.api.product.domain.ProductColor;
import com.mavis.api.product.dto.ColorVO;
import com.mavis.api.product.exception.ProductNotFoundException;
import com.mavis.api.product.repository.ProductColorRepository;
import com.mavis.api.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductReader {
    private final ProductRepository productRepository;
    private final ProductColorRepository productColorRepository;

    public Product readById(Long id) {
        return productRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> ProductNotFoundException.EXCEPTION);
    }

    public List<ColorVO> readProductColors(Long productId) {
        return productColorRepository.getProductColors(productId);
    }
}
