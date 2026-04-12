package com.mavis.admin.domains.refund.dto;

import com.mavis.admin.domains.order.dto.OrderItemInfo;
import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.domain.OrderItem;
import com.mavis.domain.domains.refund.domain.Refund;
import com.mavis.domain.domains.refund.domain.RefundImage;
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
            String receiverName,
            String receiverPhoneNumber,
            Long orderItemId,
            OrderItemInfo orderItemInfo
    ) {}

    public record RefundInfo(
            Long refundId,
            int refundAmount,
            String refundReason,
            RefundStatus refundStatus,
            String carrier,
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
                        order.getOrderAddress().getReceiverName(),
                        order.getOrderAddress().getReceiverPhone(),
                        orderItem.getId(),
                        new OrderItemInfo(orderItem.getProduct().getName(), orderItem.getColor(), orderItem.getQuantity())
                ))
                .refundInfo(new RefundInfo(
                        refund.getId(),
                        refund.getRefundAmount(),
                        refund.getRefundReason(),
                        refund.getRefundStatus(),
                        refund.getCarrier(),
                        refund.getTrackingNumber(),
                        refund.getImages().stream().map(RefundImage::getImageUrl).toList(),
                        refund.getCreatedAt()
                ))
                .build();
    }
}
