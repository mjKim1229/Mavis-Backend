package com.mavis.admin.domains.refund.dto;

import com.mavis.domain.domains.claim.domain.Claim;
import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.domain.Payment;
import com.mavis.domain.domains.order.domain.RefundReceiveAccount;

public record RefundValidateInfo(
        Long claimId,
        Long orderId,
        int refundAmount,
        String refundReason,
        String paymentKey,
        RefundReceiveAccount refundReceiveAccount
) {
    public static RefundValidateInfo from(Claim claim, Payment confirmPayment) {
        Order order = claim.getOrder();
        return new RefundValidateInfo(
                claim.getId(),
                order.getId(),
                claim.getItemsTotalPrice(),
                claim.getReason(),
                confirmPayment.getPaymentKey(),
                confirmPayment.getRefundReceiveAccount()
        );
    }
}
