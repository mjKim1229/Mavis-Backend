package com.mavis.domain.domains.order.domain;

public enum IdempotencyStatus {
    PROCESSING,
    SUCCESS,
    FAILURE,
    /**
     * 외부 결제 호출은 성공했으나 우리 DB 후처리가 실패한 상태.
     * 돈은 이미 움직였고 DB는 반영되지 않았으므로 재시도가 아니라 사람의 확인·복구가 필요하다.
     */
    NEEDS_RECONCILE
}
