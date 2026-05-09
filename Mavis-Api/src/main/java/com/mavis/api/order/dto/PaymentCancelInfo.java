package com.mavis.api.order.dto;

import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.domain.Payment;
import com.mavis.domain.domains.order.domain.RefundReceiveAccount;

public record PaymentCancelInfo(
        Long orderId,
        String paymentKey,
        RefundReceiveAccount refundReceiveAccount
) {
    public static PaymentCancelInfo from(Order order, Payment confirmPayment) {
        return new PaymentCancelInfo(
                order.getId(),
                confirmPayment.getPaymentKey(),
                confirmPayment.getRefundReceiveAccount()
        );
    }
}
