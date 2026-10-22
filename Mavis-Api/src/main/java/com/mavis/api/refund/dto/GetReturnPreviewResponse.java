package com.mavis.api.refund.dto;

import com.mavis.domain.domains.claim.domain.Claim;

public record GetReturnPreviewResponse(
        int itemsTotalPrice,
        int orderShippingFeeDeducted,
        int returnShippingFeeDeducted,
        int deductedRefundAmount,
        int sellerFaultRefundAmount,
        boolean refundable
) {
    public static GetReturnPreviewResponse of(Claim draft, boolean orderShippingSettled) {
        int itemsTotalPrice = draft.getItemsTotalPrice();
        int orderShippingFee = draft.orderShippingFee(orderShippingSettled);
        int deductedAmount = draft.deductedRefundAmount(orderShippingSettled);
        int sellerFaultRefundAmount = draft.fullRefundAmount(orderShippingSettled);
        return new GetReturnPreviewResponse(
                itemsTotalPrice, orderShippingFee, Claim.RETURN_SHIPPING_FEE,
                Math.max(0, deductedAmount), sellerFaultRefundAmount, deductedAmount >= 1);
    }
}
