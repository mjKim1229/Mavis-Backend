package com.mavis.admin.domains.order.dto;

import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.domain.OrderAddress;
import com.mavis.domain.domains.user.domain.User;
import lombok.Builder;

import java.util.List;

import static com.mavis.common.util.OrderNumberGenerator.DOMAIN_PREFIX;

@Builder
public record GetAdminOrderResponse(
        String orderId,
        List<OrderItemInfo> orderItemInfos,
        String receiverName,
        String receiverPhoneNumber,
        String address,
        String buyerName,
        String orderedAt,
        int totalPrice,
        String requestMessage
) {
    public static GetAdminOrderResponse from(Order order, OrderAddress orderAddress, String orderedAt, List<OrderItemInfo> orderItemInfos, User user) {
        return GetAdminOrderResponse.builder()
                .orderItemInfos(orderItemInfos)
                .orderId(order.getOrderId().substring(DOMAIN_PREFIX.length()))
                .orderedAt(orderedAt)
                .totalPrice(order.getTotalPrice())
                .address(orderAddress.getAddress())
                .receiverName(orderAddress.getReceiverName())
                .receiverPhoneNumber(orderAddress.getReceiverPhone())
                .requestMessage(orderAddress.getAddressMemo())
                .buyerName(user.getUsername())
                .build();
    }
}
