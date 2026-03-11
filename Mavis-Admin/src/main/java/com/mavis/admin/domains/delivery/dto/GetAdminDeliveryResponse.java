package com.mavis.admin.domains.delivery.dto;

import com.mavis.admin.domains.order.dto.OrderItemInfo;
import com.mavis.domain.domains.delivery.domain.Delivery;
import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.domain.OrderAddress;
import com.mavis.domain.domains.user.domain.User;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record GetAdminDeliveryResponse(
        String orderId,
        LocalDateTime orderedAt,
        List<OrderItemInfo> orderItemInfos,
        int totalPrice,
        String address,
        String zipCode,
        String buyerName,
        String receiverName,
        String receiverPhoneNumber,
        String requestMessage,
        String carrier,
        String trackingNumber
) {
    public static GetAdminDeliveryResponse from(
            Delivery delivery,
            Order order,
            OrderAddress orderAddress,
            List<OrderItemInfo> orderItemInfos,
            User user
    ) {
        return GetAdminDeliveryResponse.builder()
                .orderItemInfos(orderItemInfos)
                .orderId(order.getOrderId())
                .orderedAt(order.getCreatedAt())
                .totalPrice(order.getTotalPrice())
                .address(orderAddress.getAddress())
                .zipCode(orderAddress.getZipCode())
                .receiverName(orderAddress.getReceiverName())
                .receiverPhoneNumber(orderAddress.getReceiverPhone())
                .requestMessage(orderAddress.getAddressMemo())
                .buyerName(user.getUsername())
                .carrier(delivery.getCarrier())
                .trackingNumber(delivery.getTrackingNumber())
                .build();
    }
}