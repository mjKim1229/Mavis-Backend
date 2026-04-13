package com.mavis.admin.domains.order.dto;

public record AdminOrderCountResponse(
        long paymentConfirmedCount,
        long orderedCount,
        long shippedCount,
        long deliveredCount,
        long refundRequestedCount
) {
    public static AdminOrderCountResponse of(long paymentConfirmedCount, long orderedCount,
                                             long shippedCount, long deliveredCount,
                                             long refundRequestedCount) {
        return new AdminOrderCountResponse(paymentConfirmedCount, orderedCount, shippedCount, deliveredCount, refundRequestedCount);
    }
}
