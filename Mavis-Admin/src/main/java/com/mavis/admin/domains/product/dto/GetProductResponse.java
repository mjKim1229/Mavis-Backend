package com.mavis.admin.domains.product.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mavis.common.enums.ProductSubCategory;
import com.mavis.domain.domains.product.domain.Product;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record GetProductResponse(
        Long id,
        String name,
        Integer price,
        ProductSubCategory subCategory,
        List<String> colors,
        String previewImage,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDateTime createdAt
) {
    public static GetProductResponse from(Product product, List<String> colors, String previewImage) {
        return GetProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .subCategory(product.getSubCategory())
                .colors(colors)
                .previewImage(previewImage)
                .createdAt(product.getCreatedAt())
                .build();
    }
}
