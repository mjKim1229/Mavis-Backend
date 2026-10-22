ALTER TABLE claim
    ADD COLUMN reason_type VARCHAR(255) NULL COMMENT 'CHANGE_OF_MIND: 단순 변심 / SELLER_FAULT: 판매자 귀책. 고객이 반품 신청 시 선택, 취소 클레임은 NULL';

UPDATE claim
SET reason_type = 'CHANGE_OF_MIND'
WHERE claim_type = 'RETURN';

ALTER TABLE claim_item
    ADD COLUMN reason VARCHAR(255) NULL COMMENT '반품 신청 시 고객이 상품별로 입력한 사유. 취소 클레임·기존 데이터는 NULL';

ALTER TABLE refund
    ADD COLUMN order_shipping_fee_deducted INT NOT NULL DEFAULT 0 COMMENT '총액 계산 시 차감한 주문 배송비 (반품 환불에서 단순 변심/귀책 아님일 때만 값 있음). 당시 금액 스냅샷',
    ADD COLUMN return_shipping_fee_deducted INT NOT NULL DEFAULT 0 COMMENT '총액 계산 시 차감한 반품 배송비 (반품 환불에서 단순 변심/귀책 아님일 때만 값 있음). 당시 금액 스냅샷';
