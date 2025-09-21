package com.mavis.admin.product.implement;

import com.mavis.domains.product.domain.Product;
import com.mavis.domains.product.domain.ProductColor;
import com.mavis.domains.product.repository.ProductColorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductColorAppender {

    private final ProductColorRepository productColorRepository;

    public void saveProductColors(List<String> colors, Product product) {
        List<ProductColor> productColors = colors.stream()
                .map(color -> ProductColor.of(product, color))
                .toList();
        productColorRepository.saveAll(productColors);
    }
}
