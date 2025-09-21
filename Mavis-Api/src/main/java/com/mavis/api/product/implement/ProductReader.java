package com.mavis.api.product.implement;

import com.mavis.domain.domains.product.domain.Product;
import com.mavis.domain.domains.product.vo.ColorVO;
import com.mavis.domain.domains.product.vo.ProductNoticeResponse;
import com.mavis.domain.domains.product.exception.ProductNotFoundException;
import com.mavis.domain.domains.product.repository.ProductColorRepository;
import com.mavis.domain.domains.product.repository.ProductNoticeRepository;
import com.mavis.domain.domains.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductReader {
    private final ProductRepository productRepository;
    private final ProductColorRepository productColorRepository;
    private final ProductNoticeRepository productNoticeRepository;

    public Product readById(Long id) {
        return productRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> ProductNotFoundException.EXCEPTION);
    }

    public List<ColorVO> readProductColors(Long productId) {
        return productColorRepository.getProductColors(productId);
    }

    public ProductNoticeResponse readProductNotice(Long productId) {
        return productNoticeRepository.getProductNoticeByProductId(productId)
                .orElseThrow(() -> ProductNotFoundException.EXCEPTION);
    }
}
