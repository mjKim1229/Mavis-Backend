package com.mavis.domain.domains.order.domain;

public enum PaymentType {
    CONFIRM,  // 결제 승인 (가상 계좌 입금 대기 포함)
    CANCEL,   // 결제 취소 (전체/부분)
    DEPOSIT   // 가상계좌 입금 확인 (입금 완료 웹훅)
}
