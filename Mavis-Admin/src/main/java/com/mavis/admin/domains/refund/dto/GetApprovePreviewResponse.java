package com.mavis.admin.domains.refund.dto;

import com.mavis.domain.domains.claim.domain.FaultParty;
import com.mavis.domain.domains.claim.domain.ReturnRefundPlan;

public record GetApprovePreviewResponse(
        int itemsTotalPrice,
        int refundBaseAmount,
        int totalAmount,
        int shippingFeeRefund,
        int orderShippingFeeDeducted,
        int returnShippingFeeDeducted,
        int maxRefundable,
        FaultParty faultParty
) {
    public static GetApprovePreviewResponse from(RefundValidateInfo info) {
        ReturnRefundPlan plan = info.plan();
        int refundBaseAmount = plan.totalAmount() + plan.orderShippingFeeDeducted() + plan.returnShippingFeeDeducted();
        return new GetApprovePreviewResponse(
                info.itemsTotalPrice(),
                refundBaseAmount,
                plan.totalAmount(),
                plan.shippingFeeRefund(),
                plan.orderShippingFeeDeducted(),
                plan.returnShippingFeeDeducted(),
                info.maxRefundable(),
                plan.faultParty());
    }
}
