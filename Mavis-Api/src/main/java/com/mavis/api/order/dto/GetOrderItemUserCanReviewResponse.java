package com.mavis.api.order.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mavis.domain.domains.order.domain.OrderOption;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record GetOrderItemUserCanReviewResponse(
        Long orderItemId,
        String productName,
        OrderOption orderOption,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDateTime orderedAt
) {
}
