package com.mavis.admin.domains.refund.dto;

import com.mavis.admin.domains.order.dto.OrderItemInfo;
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

    public record CancelInfo(
            Long refundId,
            int refundAmount,
            String cancelReason,
            LocalDateTime canceledAt,
            PaymentMethod paymentMethod
    ) {}

    public static GetAdminCanceledRefundResponse from(Refund refund) {
        OrderItem orderItem = refund.getOrderItem();
        Order order = orderItem.getOrder();
        User user = order.getUser();
        OrderAddress orderAddress = order.getOrderAddress();
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
                        refund.getId(),
                        refund.getPayment().getCancelAmount(),
                        refund.getRefundReason(),
                        refund.getCreatedAt(),
                        refund.getPayment().getMethod()
                ))
                .build();
    }
}
