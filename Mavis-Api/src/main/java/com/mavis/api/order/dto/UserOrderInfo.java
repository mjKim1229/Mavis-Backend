package com.mavis.api.order.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mavis.domain.domains.order.domain.OrderStatus;
import lombok.Builder;

import java.time.LocalDateTime;


@Builder
public record UserOrderInfo(
        OrderStatus orderStatus,
        String address,
        String addressInfo,
        int totalPrice,
        String userName,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDateTime createdAt
) {
}
