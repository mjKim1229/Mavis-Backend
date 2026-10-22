package com.mavis.domain.domains.claim.domain;

public record ReturnRefundPlan(
        int totalAmount,
        int shippingFeeRefund,
        int orderShippingFeeDeducted,
        int returnShippingFeeDeducted,
        FaultParty faultParty
) {
}
