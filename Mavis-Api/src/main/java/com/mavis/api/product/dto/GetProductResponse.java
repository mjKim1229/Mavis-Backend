package com.mavis.api.product.dto;

import com.mavis.domain.domains.product.domain.Product;
import com.mavis.domain.domains.product.vo.ColorVO;
import lombok.Builder;

import java.util.List;

@Builder
public record GetProductResponse(
        Long id,
        String name,
        Integer price,
        List<ColorVO> colors,
        boolean isClearance,
        List<String> mainImages,
        List<String> productImages,
        List<String> detailImages
) {
    public static GetProductResponse from(Product product, List<ColorVO> colors, List<String> mainImages, List<String> productImages, List<String> detailImages) {
        return GetProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .colors(colors)
                .isClearance(product.isClearance())
                .mainImages(mainImages)
                .productImages(productImages)
                .detailImages(detailImages)
                .build();
    }
}
