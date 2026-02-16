package com.mavis.api.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateCartRequest(
        @NotNull(message = "상품 ID는 필수입니다.")
        Long productId,

        @NotNull(message = "색상은 필수입니다.")
        String color,

        @Min(value = 1, message = "수량은 1개 이상이어야 합니다.")
        int quantity
) {
}
