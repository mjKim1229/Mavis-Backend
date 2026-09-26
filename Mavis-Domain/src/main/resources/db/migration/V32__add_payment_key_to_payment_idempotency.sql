ALTER TABLE payment_idempotency
    ADD COLUMN payment_key VARCHAR(200) NULL COMMENT '외부 결제 호출 성공 후 후처리가 실패한 경우(NEEDS_RECONCILE) 복구 조회용 Toss paymentKey';
