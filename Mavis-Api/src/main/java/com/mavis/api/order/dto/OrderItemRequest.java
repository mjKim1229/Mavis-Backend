package com.mavis.api.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record OrderItemRequest(
        @NotNull(message = "상품 ID는 필수입니다.")
        Long productId,

        @NotNull(message = "옵션 정보는 필수입니다.")
        @Valid
        OrderOptionRequest option
) {
}
