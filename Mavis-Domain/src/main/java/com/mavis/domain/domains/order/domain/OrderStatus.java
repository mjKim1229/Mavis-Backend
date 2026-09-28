package com.mavis.domain.domains.order.domain;

import com.mavis.common.enums.EnumMapperType;
import lombok.RequiredArgsConstructor;

import java.util.Set;

// 일반 결제: READY → PAYMENT_REQUESTED → PAYMENT_CONFIRMED → ORDERED
// 가상계좌: READY → PAYMENT_REQUESTED → WAITING_FOR_DEPOSIT → PAYMENT_CONFIRMED → ORDERED
// 취소: PAYMENT_REQUESTED / WAITING_FOR_DEPOSIT / PAYMENT_CONFIRMED → CANCELED (READY 취소는 호출부 없음)
// 발주(ORDERED) 이후 취소 불가 — 배송 완료 후 반품(ClaimType.RETURN) 흐름으로 처리
@RequiredArgsConstructor
public enum OrderStatus implements EnumMapperType {
    READY("주문 준비"),                    // 주문 생성 초기 상태
    PAYMENT_REQUESTED("결제 요청"),        // 결제창 진입 (일반 결제)
    WAITING_FOR_DEPOSIT("입금 대기"),      // 가상계좌 발급 후 입금 대기
    PAYMENT_CONFIRMED("결제 완료"),        // 결제 완료 (일반: 승인 후, 가상계좌: 입금 확인 후)
    ORDERED("발주 완료"),                  // 어드민 발주 처리 완료
    CANCELED("주문 취소");                 // 결제 취소

    private final String title;

    private static final Set<OrderStatus> FROM_READY = Set.of(PAYMENT_REQUESTED);
    // CANCELED 전이는 결제 보정 스케줄러(PaymentReconciliationService.reconcileReadyOrder) 전용 — 현재 비활성.
    // 사용자 취소 API는 PAYMENT_CONFIRMED/WAITING_FOR_DEPOSIT만 허용하므로 이 경로로 들어오지 않는다.
    private static final Set<OrderStatus> FROM_PAYMENT_REQUESTED = Set.of(WAITING_FOR_DEPOSIT, PAYMENT_CONFIRMED, CANCELED);
    private static final Set<OrderStatus> FROM_WAITING_FOR_DEPOSIT = Set.of(PAYMENT_CONFIRMED, CANCELED);
    private static final Set<OrderStatus> FROM_PAYMENT_CONFIRMED = Set.of(ORDERED, CANCELED);

    public boolean canTransitionTo(OrderStatus next) {
        Set<OrderStatus> allowed = allowedNextStatuses();
        return allowed.contains(next);
    }

    private Set<OrderStatus> allowedNextStatuses() {
        return switch (this) {
            case READY -> FROM_READY;
            case PAYMENT_REQUESTED -> FROM_PAYMENT_REQUESTED;
            case WAITING_FOR_DEPOSIT -> FROM_WAITING_FOR_DEPOSIT;
            case PAYMENT_CONFIRMED -> FROM_PAYMENT_CONFIRMED;
            case ORDERED, CANCELED -> Set.of();
        };
    }

    @Override
    public String getCode() {
        return name();
    }

    @Override
    public String getTitle() {
        return title;
    }
}
