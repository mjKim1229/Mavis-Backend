package com.mavis.domain.domains.claim.domain;

public record ReturnRefundPlan(
        int totalAmount,
        // 총액 중 돌려주는 주문 배송비 (판매자 귀책 전액 환불의 첫 정산만 값 있음)
        int shippingFeeRefund,
        // 상품 금액에서 뺀 주문 배송비 (변심·귀책 아님의 첫 정산만 값 있음)
        int orderShippingFeeDeducted,
        // 상품 금액에서 뺀 반품 배송비 (변심·귀책 아님이면 항상 값 있음)
        int returnShippingFeeDeducted,
        FaultParty faultParty
) {
}
