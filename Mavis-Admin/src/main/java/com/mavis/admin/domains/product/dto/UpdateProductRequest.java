package com.mavis.admin.domains.product.dto;

import com.mavis.common.enums.ProductSubCategory;

import java.util.List;

public record UpdateProductRequest(
        String name,
        Integer price,
        ProductSubCategory subCategory,
        List<String> colors,
        ProductNoticeVO notice
) {
}
