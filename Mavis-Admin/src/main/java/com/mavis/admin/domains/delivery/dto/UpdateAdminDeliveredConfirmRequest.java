package com.mavis.admin.domains.delivery.dto;

import com.mavis.domain.domains.delivery.domain.DeliveryStatus;

import java.util.List;

public record UpdateAdminDeliveredConfirmRequest(
        List<Long> deliverIds,
        DeliveryStatus deliveryStatus
) {
}
