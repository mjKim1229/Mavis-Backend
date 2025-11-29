package com.mavis.admin.domains.product.dto;

import com.mavis.common.enums.ProductCategory;
import com.mavis.common.enums.ProductSubCategory;
import com.mavis.domain.domains.product.domain.Product;
import com.mavis.domain.domains.product.vo.ColorVO;
import jdk.jfr.Category;
import lombok.Builder;

import java.util.List;

@Builder
public record GetProductResponse(
        Long id,
        String name,
        Integer price,
        List<ColorVO> colors,
        boolean isClearance,
        ProductCategory category,
        ProductSubCategory subCategory,
        List<String> mainImages,
        List<String> productImages,
        List<String> detailImages,
        ProductNoticeVO productNoticeVO
) {
    public static GetProductResponse from(Product product, List<ColorVO> colors, List<String> mainImages, List<String> productImages, List<String> detailImages, ProductNoticeVO productNoticeVO) {
        return GetProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .colors(colors)
                .isClearance(product.isClearance())
                .category(ProductCategory.fromSubCategory(product.getSubCategory()))
                .subCategory(product.getSubCategory())
                .mainImages(mainImages)
                .productImages(productImages)
                .detailImages(detailImages)
                .productNoticeVO(productNoticeVO)
                .build();
    }
}
