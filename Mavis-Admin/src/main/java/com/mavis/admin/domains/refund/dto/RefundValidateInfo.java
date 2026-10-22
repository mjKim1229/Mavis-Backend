package com.mavis.admin.domains.refund.dto;

import com.mavis.domain.domains.claim.domain.Claim;
import com.mavis.domain.domains.claim.domain.ReturnRefundPlan;
import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.domain.Payment;
import com.mavis.domain.domains.order.domain.RefundReceiveAccount;

public record RefundValidateInfo(
        Long claimId,
        Long orderId,
        int itemsTotalPrice,
        int maxRefundable,
        ReturnRefundPlan plan,
        String refundReason,
        String paymentKey,
        RefundReceiveAccount refundReceiveAccount
) {
    public static RefundValidateInfo from(Claim claim, Payment confirmPayment, int maxRefundable, ReturnRefundPlan plan) {
        Order order = claim.getOrder();
        return new RefundValidateInfo(
                claim.getId(),
                order.getId(),
                claim.getItemsTotalPrice(),
                maxRefundable,
                plan,
                claim.getReason(),
                confirmPayment.getPaymentKey(),
                confirmPayment.getRefundReceiveAccount()
        );
    }

    public int refundAmount() {
        return plan.totalAmount();
    }
}
