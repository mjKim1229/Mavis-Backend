package com.mavis.admin.domains.order.dto;

public record AdminDeliveryStartRequest(
        String carrier,
        String trackingNumber
) {
}