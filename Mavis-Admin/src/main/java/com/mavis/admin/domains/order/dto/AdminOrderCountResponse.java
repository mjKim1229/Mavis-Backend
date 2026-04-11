package com.mavis.admin.domains.order.dto;

public record AdminOrderCountResponse(
        long paymentConfirmedCount,
        long orderedCount,
        long shippedCount,
        long deliveredCount
) {
    public static AdminOrderCountResponse of(long paymentConfirmedCount, long orderedCount,
                                             long shippedCount, long deliveredCount) {
        return new AdminOrderCountResponse(paymentConfirmedCount, orderedCount, shippedCount, deliveredCount);
    }
}
