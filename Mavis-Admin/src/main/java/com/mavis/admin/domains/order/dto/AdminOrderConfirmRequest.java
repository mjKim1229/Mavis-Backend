package com.mavis.admin.domains.order.dto;

import java.util.List;

public record AdminOrderConfirmRequest(
        List<Long> orderIds
) {
}
