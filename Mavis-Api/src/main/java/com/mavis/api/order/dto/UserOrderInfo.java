package com.mavis.api.order.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mavis.domain.domains.order.domain.Order;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

import static com.mavis.common.util.OrderNumberGenerator.DOMAIN_PREFIX;

@Builder
public record UserOrderInfo(
        Long orderId,
        String tossOrderId,
        List<OrderProduct> orderProductList,
        String orderStatus,
        String address,
        String addressInfo,
        int totalPrice,
        String userName,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDateTime createdAt
) {
    public static UserOrderInfo from(Order order) {
        return UserOrderInfo.builder()
                .orderId(order.getId())
                .tossOrderId(order.getOrderId().substring(DOMAIN_PREFIX.length()))
                .orderProductList(order.getOrderItems().stream().map(OrderProduct::from).toList())
                .orderStatus(order.getDisplayStatus())
                .address(order.getOrderAddress().getAddress())
                .addressInfo(order.getOrderAddress().getAddressDetail())
                .totalPrice(order.getTotalPrice())
                .userName(order.getUser().getName())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
