package com.mavis.api.order.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record UserOrderInfo(
        Long id,
        String orderId,
        List<OrderProduct> orderProductList,
        String orderStatus,
        String address,
        String addressInfo,
        int totalPrice,
        String userName,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDateTime createdAt
) {
}
