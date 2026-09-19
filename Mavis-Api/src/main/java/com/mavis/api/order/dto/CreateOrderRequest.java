package com.mavis.api.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record CreateOrderRequest(
        @Positive(message = "결제 금액은 0보다 커야 합니다.")
        int amount,

        @NotNull(message = "배송지 정보는 필수입니다.")
        @Valid
        OrderAddressRequest orderAddressRequest,

        @NotEmpty(message = "주문 상품은 1개 이상이어야 합니다.")
        @Valid
        List<OrderItemRequest> orderItems
) {
}
