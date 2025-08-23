package com.mavis.api.product.dto;

import com.mavis.common.enums.EnumMapperVO;
import com.mavis.common.enums.ProductCategory;

import java.util.List;

public record SubCategoryVO(
        String code,
        String title,
        List<EnumMapperVO> subCategories
) {
    public static SubCategoryVO from(ProductCategory category, List<EnumMapperVO> subCategories) {
        return new SubCategoryVO(category.getCode(), category.getTitle(), subCategories);
    }
}
