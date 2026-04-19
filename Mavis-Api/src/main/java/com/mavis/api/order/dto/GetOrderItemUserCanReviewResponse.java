package com.mavis.api.order.dto;

import com.mavis.domain.domains.order.domain.OrderOption;
import lombok.Builder;

@Builder
public record GetOrderItemUserCanReviewResponse(
        Long orderItemId,
        String productName,
        OrderOption orderOption,
        String orderedAt
) {
}
