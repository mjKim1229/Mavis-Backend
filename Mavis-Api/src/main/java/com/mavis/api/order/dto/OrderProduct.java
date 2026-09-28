package com.mavis.api.order.dto;

import com.mavis.domain.domains.claim.domain.ClaimStatus;
import com.mavis.domain.domains.order.domain.OrderOption;
import com.mavis.domain.domains.order.dto.OrderProductRow;


public record OrderProduct(
        Long orderItemId,
        Long productId,
        String productName,
        OrderOption option,
        int totalPrice,
        ClaimStatus refundStatus,
        String refundStatusTitle,
        Integer refundAmount,
        String productImageUrl
) {
    public static OrderProduct from(OrderProductRow row) {
        ClaimStatus refundStatus = row.refundStatus();
        String refundStatusTitle = refundStatus != null ? refundStatus.getTitle() : null;
        OrderOption option = new OrderOption(row.color(), row.quantity());
        int totalPrice = row.totalPrice();
        Integer refundAmount = resolveRefundAmount(row);
        return new OrderProduct(
                row.orderItemId(),
                row.productId(),
                row.productName(),
                option,
                totalPrice,
                refundStatus,
                refundStatusTitle,
                refundAmount,
                row.productImageUrl()
        );
    }

    // 반품 완료면 실제 환불액, 처리 전·거절이면 상품 금액(환불 예정액)
    private static Integer resolveRefundAmount(OrderProductRow row) {
        if (row.refundStatus() == null) {
            return null;
        }
        if (row.refundAmount() != null) {
            return row.refundAmount();
        }
        return row.totalPrice();
    }
}
