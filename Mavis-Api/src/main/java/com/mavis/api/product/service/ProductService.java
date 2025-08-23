package com.mavis.api.product.service;

import com.mavis.api.product.domain.Product;
import com.mavis.api.product.dto.ColorVO;
import com.mavis.api.product.dto.GetProductResponse;
import com.mavis.api.product.implement.ProductReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductReader productReader;

    public GetProductResponse getProductById(Long id) {
        Product product = productReader.readById(id);
        List<ColorVO> colors = productReader.readProductColors(product.getId());
        return GetProductResponse.from(product, colors);
    }
}
