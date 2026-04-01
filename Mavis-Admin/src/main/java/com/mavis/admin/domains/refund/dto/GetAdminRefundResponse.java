package com.mavis.admin.domains.refund.dto;

import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.domain.OrderItem;
import com.mavis.domain.domains.refund.domain.Refund;
import com.mavis.domain.domains.refund.domain.RefundStatus;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

import static com.mavis.common.util.OrderNumberGenerator.DOMAIN_PREFIX;

@Builder
public record GetAdminRefundResponse(
        Long refundId,
        Long orderItemId,
        String tossOrderId,
        String productName,
        String color,
        int refundQuantity,
        int refundAmount,
        String refundReason,
        RefundStatus refundStatus,
        String userName,
        String trackingNumber,
        List<String> imageUrls,
        LocalDateTime requestedAt
) {
    public static GetAdminRefundResponse from(Refund refund) {
        OrderItem orderItem = refund.getOrderItem();
        Order order = orderItem.getOrder();
        return GetAdminRefundResponse.builder()
                .refundId(refund.getId())
                .orderItemId(orderItem.getId())
                .tossOrderId(order.getOrderId().substring(DOMAIN_PREFIX.length()))
                .productName(orderItem.getProduct().getName())
                .color(orderItem.getColor())
                .refundQuantity(refund.getRefundQuantity())
                .refundAmount(refund.getRefundAmount())
                .refundReason(refund.getRefundReason())
                .refundStatus(refund.getRefundStatus())
                .userName(order.getUser().getName())
                .trackingNumber(refund.getTrackingNumber())
                .imageUrls(refund.getImages().stream().map(image -> image.getImageUrl()).toList())
                .requestedAt(refund.getCreatedAt())
                .build();
    }
}
