package com.mavis.api.order.dto;

import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.domain.OrderStatus;
import com.mavis.domain.domains.order.domain.Payment;
import com.mavis.domain.domains.order.domain.RefundReceiveAccount;

public record PaymentCancelInfo(
        Long orderId,
        String paymentKey,
        RefundReceiveAccount refundReceiveAccount
) {
    public static PaymentCancelInfo from(Order order, Payment confirmPayment) {
        // 가상계좌 입금 전(WAITING_FOR_DEPOSIT) 취소는 refundReceiveAccount 없이 요청해야 함 (Toss 정책)
        RefundReceiveAccount refundReceiveAccount = order.getOrderStatus() == OrderStatus.PAYMENT_CONFIRMED
                ? confirmPayment.getRefundReceiveAccount()
                : null;
        return new PaymentCancelInfo(
                order.getId(),
                confirmPayment.getPaymentKey(),
                refundReceiveAccount
        );
    }
}
