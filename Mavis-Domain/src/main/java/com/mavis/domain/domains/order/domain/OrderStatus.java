package com.mavis.domain.domains.order.domain;

import com.mavis.common.enums.EnumMapperType;
import lombok.RequiredArgsConstructor;

// 일반 결제: READY → PAYMENT_REQUESTED → PAYMENT_CONFIRMED → ORDERED
// 가상계좌: READY → WAITING_FOR_DEPOSIT → PAYMENT_CONFIRMED → ORDERED
// 취소: PAYMENT_CONFIRMED → CANCELED
@RequiredArgsConstructor
public enum OrderStatus implements EnumMapperType {
    READY("주문 준비"),                    // 주문 생성 초기 상태
    PAYMENT_REQUESTED("결제 요청"),        // 결제창 진입 (일반 결제)
    WAITING_FOR_DEPOSIT("입금 대기"),      // 가상계좌 발급 후 입금 대기
    PAYMENT_CONFIRMED("결제 완료"),        // 결제 완료 (일반: 승인 후, 가상계좌: 입금 확인 후)
    ORDERED("발주 완료"),                  // 어드민 발주 처리 완료
    CANCELED("주문 취소");                 // 결제 취소

    private final String title;

    @Override
    public String getCode() {
        return name();
    }

    @Override
    public String getTitle() {
        return title;
    }
}
