package com.mavis.domain.domains.delivery.dto;

import com.mavis.domain.domains.delivery.domain.DeliveryStatus;

import java.time.LocalDateTime;

public record AdminDeliveryRow(
        Long deliveryId,
        String tossOrderId,
        String carrier,
        String trackingNumber,
        DeliveryStatus deliveryStatus,
        Long orderId,
        String receiverName,
        String receiverPhone,
        String address,
        String addressMemo,
        String buyerName,
        LocalDateTime createdAt,
        int totalPrice
) {
}
