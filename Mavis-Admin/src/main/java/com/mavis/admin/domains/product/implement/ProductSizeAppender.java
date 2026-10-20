package com.mavis.admin.domains.product.implement;

import com.mavis.common.enums.Size;
import com.mavis.domain.domains.product.domain.Product;
import com.mavis.domain.domains.product.domain.ProductSize;
import com.mavis.domain.domains.product.repository.ProductSizeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductSizeAppender {

    private final ProductSizeRepository productSizeRepository;

    public void saveProductSizes(List<Size> sizes, Product product) {
        List<ProductSize> productSizes = sizes.stream()
                .map(size -> ProductSize.of(product, size))
                .toList();
        productSizeRepository.saveAll(productSizes);
    }
}
