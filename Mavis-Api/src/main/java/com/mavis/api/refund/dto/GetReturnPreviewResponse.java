package com.mavis.api.refund.dto;

import com.mavis.domain.domains.claim.domain.Claim;
import com.mavis.domain.domains.claim.domain.ClaimReasonType;

public record GetReturnPreviewResponse(
        int itemsTotalPrice,
        int orderShippingFeeDeducted,
        int returnShippingFeeDeducted,
        int expectedRefundAmount,
        boolean refundable,
        Integer sellerFaultRefundAmount
) {
    public static GetReturnPreviewResponse of(Claim draft, boolean orderShippingSettled) {
        int itemsTotalPrice = draft.getItemsTotalPrice();
        int orderShippingFee = draft.orderShippingFee(orderShippingSettled);
        int deductedAmount = draft.deductedRefundAmount(orderShippingSettled);
        int expectedRefundAmount = Math.max(0, deductedAmount);
        boolean refundable = deductedAmount >= 1;
        Integer sellerFaultRefundAmount = draft.getReasonType() == ClaimReasonType.SELLER_FAULT
                ? draft.fullRefundAmount(orderShippingSettled)
                : null;
        return new GetReturnPreviewResponse(
                itemsTotalPrice, orderShippingFee, Claim.RETURN_SHIPPING_FEE,
                expectedRefundAmount, refundable, sellerFaultRefundAmount);
    }
}
