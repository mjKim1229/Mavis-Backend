package com.mavis.admin.domains.delivery.dto;

import java.util.List;

public record AdminCompleteDeliveryRequest(
        List<Long> deliverIds
) {
}
