package com.mavis.admin.domains.product.dto;

import com.mavis.common.enums.ProductSubCategory;
import com.mavis.common.enums.Size;

import java.util.List;

public record UpdateProductRequest(
        String name,
        Integer price,
        ProductSubCategory subCategory,
        List<String> colors,
        List<Size> sizes,
        List<ProductImageVO> mainImages,
        List<ProductImageVO> productImages,
        List<ProductImageVO> detailImages,
        ProductNoticeVO notice
) {
}
