package com.mavis.api.product.dto;

import com.mavis.api.product.domain.Product;
import lombok.Builder;

import java.util.List;

@Builder
public record GetProductResponse(
        Long id,
        String name,
        Integer price,
        List<ColorVO> colors,
        List<String> imageUrls
) {
    public static GetProductResponse from(Product product, List<ColorVO> colors, List<String> imageUrls) {
        return GetProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .colors(colors)
                .imageUrls(imageUrls)
                .build();
    }
}
