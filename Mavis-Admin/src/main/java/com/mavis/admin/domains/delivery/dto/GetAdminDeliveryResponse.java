package com.mavis.admin.domains.delivery.dto;

import com.mavis.admin.domains.order.dto.OrderItemInfo;
import com.mavis.domain.domains.delivery.domain.Delivery;
import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.domain.OrderAddress;
import com.mavis.domain.domains.user.domain.User;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

import static com.mavis.common.util.OrderNumberGenerator.DOMAIN_PREFIX;

@Builder
public record GetAdminDeliveryResponse(
        Long deliveryId,
        String orderId,
        String carrier,
        String trackingNumber,
        List<OrderItemInfo> orderItemInfos,
        String receiverName,
        String receiverPhoneNumber,
        String address,
        int totalPrice,
        String deliveryStatus,
        String buyerName,
        String orderedAt,
        String requestMessage
) {
    public static GetAdminDeliveryResponse from(
            Delivery delivery,
            Order order,
            String orderedAt,
            OrderAddress orderAddress,
            List<OrderItemInfo> orderItemInfos,
            User user
    ) {
        return GetAdminDeliveryResponse.builder()
                .deliveryId(delivery.getId())
                .orderItemInfos(orderItemInfos)
                .orderId(order.getOrderId().substring(DOMAIN_PREFIX.length()))
                .orderedAt(orderedAt)
                .totalPrice(order.getTotalPrice())
                .address(orderAddress.getAddress())
                .receiverName(orderAddress.getReceiverName())
                .receiverPhoneNumber(orderAddress.getReceiverPhone())
                .requestMessage(orderAddress.getAddressMemo())
                .buyerName(user.getName())
                .carrier(delivery.getCarrier())
                .trackingNumber(delivery.getTrackingNumber())
                .deliveryStatus(delivery.getDeliveryStatus().getTitle())
                .build();
    }
}