package com.mavis.admin.domains.order.dto;

public record UpdateAdminOrderConfirmRequest(
        String carrier,
        String trackingNumber
) {
}