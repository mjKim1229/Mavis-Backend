package com.mavis.admin.domains.refund.dto;

import com.mavis.domain.domains.order.domain.Payment;
import com.mavis.domain.domains.order.domain.RefundReceiveAccount;
import com.mavis.domain.domains.refund.domain.Refund;

public record RefundValidateInfo(
        Long refundId,
        Long orderId,
        int refundAmount,
        String refundReason,
        String paymentKey,
        RefundReceiveAccount refundReceiveAccount
) {
    public static RefundValidateInfo from(Refund refund, Payment confirmPayment) {
        return new RefundValidateInfo(
                refund.getId(),
                refund.getOrderItem().getOrder().getId(),
                refund.getRefundAmount(),
                refund.getRefundReason(),
                confirmPayment.getPaymentKey(),
                confirmPayment.getRefundReceiveAccount()
        );
    }
}
