package com.mavis.admin.domains.refund.dto;

import com.mavis.admin.domains.order.dto.OrderItemInfo;
import com.mavis.domain.domains.claim.domain.Claim;
import com.mavis.domain.domains.claim.domain.ClaimItem;
import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.domain.OrderAddress;
import com.mavis.domain.domains.order.domain.OrderItem;
import com.mavis.domain.domains.order.domain.PaymentMethod;
import com.mavis.domain.domains.refund.domain.Refund;
import com.mavis.domain.domains.user.domain.User;
import lombok.Builder;

import java.time.LocalDateTime;

import static com.mavis.common.util.OrderNumberGenerator.DOMAIN_PREFIX;

@Builder
public record GetAdminCanceledRefundResponse(
        OrderInfo orderInfo,
        CancelInfo cancelInfo
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

    // 상품 단위 행 — refundId: FE 호환을 위해 필드명 유지, 값은 행마다 고유한 claim_item.id
    public record CancelInfo(
            Long refundId,
            int refundAmount,
            String cancelReason,
            LocalDateTime canceledAt,
            String paymentMethod,
            String paymentMethodTitle
    ) {}

    public static GetAdminCanceledRefundResponse from(ClaimItem claimItem, Refund refund) {
        Claim claim = claimItem.getClaim();
        OrderItem orderItem = claimItem.getOrderItem();
        Order order = claim.getOrder();
        User user = order.getUser();
        OrderAddress orderAddress = order.getOrderAddress();
        PaymentMethod paymentMethod = refund.getPayment().getMethod();
        return GetAdminCanceledRefundResponse.builder()
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
                .cancelInfo(new CancelInfo(
                        claimItem.getId(),
                        refund.getTotalAmount(),
                        claim.getReason(),
                        claim.getCreatedAt(),
                        paymentMethod.name(),
                        paymentMethod.getKr()
                ))
                .build();
    }
}
