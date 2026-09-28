package com.mavis.admin.domains.refund.dto;

import com.mavis.admin.domains.order.dto.OrderItemInfo;
import com.mavis.domain.domains.claim.domain.Claim;
import com.mavis.domain.domains.claim.domain.ClaimImage;
import com.mavis.domain.domains.claim.domain.ClaimItem;
import com.mavis.domain.domains.claim.domain.ClaimReturn;
import com.mavis.domain.domains.claim.domain.ClaimStatus;
import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.domain.OrderAddress;
import com.mavis.domain.domains.order.domain.OrderItem;
import com.mavis.domain.domains.refund.domain.Refund;
import com.mavis.domain.domains.user.domain.User;
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
            String receiverName,
            String receiverPhoneNumber,
            String address,
            LocalDateTime orderCreatedAt,
            Long orderItemId,
            OrderItemInfo orderItemInfo
    ) {}

    // refundId: FE 호환을 위해 필드명 유지, 값은 claim.id (승인/거절 경로 변수)
    public record RefundInfo(
            Long refundId,
            int refundAmount,
            String refundReason,
            ClaimStatus refundStatus,
            String carrier,
            String trackingNumber,
            List<String> imageUrls,
            LocalDateTime requestedAt
    ) {}

    // 반품 신청은 상품 단위라 반품 클레임의 상품은 항상 1개
    public static GetAdminRefundResponse from(Claim claim, ClaimReturn claimReturn, Refund refund) {
        List<ClaimItem> items = claim.getItems();
        ClaimItem claimItem = items.get(0);
        OrderItem orderItem = claimItem.getOrderItem();
        Order order = claim.getOrder();
        User user = order.getUser();
        OrderAddress orderAddress = order.getOrderAddress();
        int refundAmount = refund != null ? refund.getTotalAmount() : claim.getItemsTotalPrice();
        String carrier = claimReturn != null ? claimReturn.getCarrier() : null;
        String trackingNumber = claimReturn != null ? claimReturn.getTrackingNumber() : null;
        List<String> imageUrls = claim.getImages().stream()
                .map(ClaimImage::getImageUrl)
                .toList();
        return GetAdminRefundResponse.builder()
                .orderInfo(new OrderInfo(
                        order.getOrderId().substring(DOMAIN_PREFIX.length()),
                        user.getName(),
                        orderAddress.getReceiverName(),
                        orderAddress.getReceiverPhone(),
                        orderAddress.getFullAddress(),
                        order.getCreatedAt(),
                        orderItem.getId(),
                        new OrderItemInfo(orderItem.getProduct().getName(), orderItem.getColor(), orderItem.getQuantity())
                ))
                .refundInfo(new RefundInfo(
                        claim.getId(),
                        refundAmount,
                        claim.getReason(),
                        claim.getClaimStatus(),
                        carrier,
                        trackingNumber,
                        imageUrls,
                        claim.getCreatedAt()
                ))
                .build();
    }
}
