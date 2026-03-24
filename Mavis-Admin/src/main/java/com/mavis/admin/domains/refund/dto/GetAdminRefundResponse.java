package com.mavis.admin.domains.refund.dto;

import com.mavis.domain.domains.refund.domain.Refund;
import com.mavis.domain.domains.refund.domain.RefundStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record GetAdminRefundResponse(
        Long refundId,
        Long orderItemId,
        String productName,
        String color,
        int orderQuantity,
        int refundQuantity,
        int refundAmount,
        String refundReason,
        RefundStatus refundStatus,
        String userName,
        LocalDateTime requestedAt
) {
    public static GetAdminRefundResponse from(Refund refund) {
        var orderItem = refund.getOrderItem();
        return GetAdminRefundResponse.builder()
                .refundId(refund.getId())
                .orderItemId(orderItem.getId())
                .productName(orderItem.getProduct().getName())
                .color(orderItem.getColor())
                .orderQuantity(orderItem.getQuantity())
                .refundQuantity(refund.getRefundQuantity())
                .refundAmount(refund.getRefundAmount())
                .refundReason(refund.getRefundReason())
                .refundStatus(refund.getRefundStatus())
                .userName(orderItem.getOrder().getUser().getName())
                .requestedAt(refund.getCreatedAt())
                .build();
    }
}
