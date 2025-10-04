package com.mavis.api.product.dto;

import com.mavis.domain.domains.product.domain.Product;
import lombok.Builder;

import java.util.List;

@Builder
public record GetProductPreviewResponse(
        Long id,
        String name,
        Integer price,
        List<String> colors,
        String previewImage
) {
    public static GetProductPreviewResponse from(Product product, List<String> colors, String previewImage) {
        return GetProductPreviewResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .colors(colors)
                .previewImage(previewImage)
                .build();
    }
}
