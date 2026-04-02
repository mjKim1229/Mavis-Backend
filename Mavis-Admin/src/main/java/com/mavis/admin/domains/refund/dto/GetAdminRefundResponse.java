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
        OrderInfo orderInfo,
        RefundInfo refundInfo
) {
    public record OrderInfo(
            String tossOrderId,
            String userName,
            Long orderItemId,
            String productName,
            String color
    ) {}

    public record RefundInfo(
            Long refundId,
            int refundQuantity,
            int refundAmount,
            String refundReason,
            RefundStatus refundStatus,
            String trackingNumber,
            List<String> imageUrls,
            LocalDateTime requestedAt
    ) {}

    public static GetAdminRefundResponse from(Refund refund) {
        OrderItem orderItem = refund.getOrderItem();
        Order order = orderItem.getOrder();
        return GetAdminRefundResponse.builder()
                .orderInfo(new OrderInfo(
                        order.getOrderId().substring(DOMAIN_PREFIX.length()),
                        order.getUser().getName(),
                        orderItem.getId(),
                        orderItem.getProduct().getName(),
                        orderItem.getColor()
                ))
                .refundInfo(new RefundInfo(
                        refund.getId(),
                        refund.getRefundQuantity(),
                        refund.getRefundAmount(),
                        refund.getRefundReason(),
                        refund.getRefundStatus(),
                        refund.getTrackingNumber(),
                        refund.getImages().stream().map(image -> image.getImageUrl()).toList(),
                        refund.getCreatedAt()
                ))
                .build();
    }
}
