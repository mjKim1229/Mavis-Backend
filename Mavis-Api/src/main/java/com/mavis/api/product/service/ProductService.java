package com.mavis.api.product.service;

import com.mavis.api.product.domain.Product;
import com.mavis.api.product.domain.ProductNotice;
import com.mavis.api.product.dto.ColorVO;
import com.mavis.api.product.dto.GetProductResponse;
import com.mavis.api.product.dto.ProductNoticeResponse;
import com.mavis.api.product.dto.SubCategoryVO;
import com.mavis.api.product.implement.ProductReader;
import com.mavis.common.enums.EnumMapper;
import com.mavis.common.enums.ProductCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final EnumMapper enumMapper;
    private final ProductReader productReader;

    public GetProductResponse getProductById(Long id) {
        Product product = productReader.readById(id);
        List<ColorVO> colors = productReader.readProductColors(product.getId());
        return GetProductResponse.from(product, colors);
    }

    public List<SubCategoryVO> getProductTotalCategory() {
        return Arrays.stream(ProductCategory.values())
                .map(category ->
                        SubCategoryVO.from(
                                category, enumMapper.toEnumVoList(category.getSubCategories())
                        )
                )
                .toList();
    }

    public ProductNoticeResponse getProductNotice(Long productId) {
        return productReader.readProductNotice(productId);
    }
}
