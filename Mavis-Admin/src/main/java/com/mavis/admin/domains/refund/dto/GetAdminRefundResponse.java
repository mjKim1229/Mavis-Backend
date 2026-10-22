package com.mavis.admin.domains.refund.dto;

import com.mavis.admin.domains.order.dto.OrderItemInfo;
import com.mavis.domain.domains.claim.domain.Claim;
import com.mavis.domain.domains.claim.domain.ClaimImage;
import com.mavis.domain.domains.claim.domain.ClaimItem;
import com.mavis.domain.domains.claim.domain.ClaimReasonType;
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
            OrderItemInfo orderItemInfo,
            List<ReturnItem> orderItems
    ) {}

    public record ReturnItem(
            Long orderItemId,
            OrderItemInfo orderItemInfo,
            String reason
    ) {}

    // refundId: FE 호환을 위해 필드명 유지, 값은 claim.id (승인/거절 경로 변수)
    public record RefundInfo(
            Long refundId,
            int refundAmount,
            int fullRefundAmount,
            ClaimReasonType reasonType,
            String refundReason,
            ClaimStatus refundStatus,
            List<String> imageUrls,
            LocalDateTime requestedAt
    ) {}

    public static GetAdminRefundResponse from(Claim claim, Refund refund, boolean orderShippingSettled) {
        List<ClaimItem> items = claim.getItems();
        Order order = claim.getOrder();
        User user = order.getUser();
        OrderAddress orderAddress = order.getOrderAddress();
        int deductedAmount = Math.max(0, claim.deductedRefundAmount(orderShippingSettled));
        int refundAmount = refund != null ? refund.getTotalAmount() : deductedAmount;
        int fullRefundAmount = claim.fullRefundAmount(orderShippingSettled);
        List<ReturnItem> returnItems = items.stream()
                .map(GetAdminRefundResponse::toReturnItem)
                .toList();
        ReturnItem firstItem = returnItems.get(0);
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
                        firstItem.orderItemId(),
                        firstItem.orderItemInfo(),
                        returnItems
                ))
                .refundInfo(new RefundInfo(
                        claim.getId(),
                        refundAmount,
                        fullRefundAmount,
                        claim.getReasonType(),
                        claim.getReason(),
                        claim.getClaimStatus(),
                        imageUrls,
                        claim.getCreatedAt()
                ))
                .build();
    }

    private static ReturnItem toReturnItem(ClaimItem claimItem) {
        OrderItem orderItem = claimItem.getOrderItem();
        OrderItemInfo info = new OrderItemInfo(orderItem.getProduct().getName(), orderItem.getColor(), orderItem.getSize(), orderItem.getQuantity());
        return new ReturnItem(orderItem.getId(), info, claimItem.getReason());
    }
}
