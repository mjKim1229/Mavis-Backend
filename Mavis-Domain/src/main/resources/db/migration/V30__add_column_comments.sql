-- 의미가 모호한 컬럼에 코멘트 추가. 컬럼 정의(타입·NULL·DEFAULT)는 기존과 동일하게 유지
-- 엔티티 @Comment와 같은 문장으로 관리

ALTER TABLE orders
    MODIFY COLUMN order_id VARCHAR(255) NULL COMMENT 'Toss PG orderId (GARAM 접두어 포함). 내부 PK는 id — payment.order_id는 이 컬럼이 아니라 orders.id 참조',
    MODIFY COLUMN delivery_fee INT NULL COMMENT '배송비 (현재 고정 4000원). total_price에 포함됨';

ALTER TABLE order_item
    MODIFY COLUMN unit_price INT NOT NULL DEFAULT 0 COMMENT '주문 시점 상품 단가 (product.price 스냅샷)',
    MODIFY COLUMN total_price INT NOT NULL COMMENT 'unit_price × quantity. 배송비 미포함';

ALTER TABLE payment
    MODIFY COLUMN order_id BIGINT NULL COMMENT 'FK → orders.id (내부 PK). Toss orderId는 toss_order_id',
    MODIFY COLUMN toss_order_id VARCHAR(255) NULL COMMENT 'Toss PG orderId. orders.order_id와 같은 값',
    MODIFY COLUMN total_amount BIGINT NULL COMMENT 'payment_type별 의미 다름 — CONFIRM: 결제 승인 총액 / CANCEL: 이번 취소 금액 (전체취소=orders.total_price 배송비 포함, 반품=refund.refund_amount 배송비 제외) / DEPOSIT: 0. 합산 시 payment_type 필터 필수',
    MODIFY COLUMN cancel_amount INT NULL COMMENT 'CANCEL 행만 값 있음: 이번 취소 금액 (= total_amount. 전체취소=배송비 포함, 반품=배송비 제외). 그 외 NULL',
    MODIFY COLUMN balance_amount BIGINT NULL COMMENT '해당 이벤트 직후 Toss 잔여 취소가능 금액. DEPOSIT 행은 NULL',
    MODIFY COLUMN last_transaction_key VARCHAR(200) NULL COMMENT 'Toss 거래 키 — CONFIRM: 승인 / CANCEL: 취소 거래 / DEPOSIT: 입금 웹훅',
    MODIFY COLUMN virtual_account_secret VARCHAR(255) NULL COMMENT 'Toss 가상계좌 입금 웹훅 검증용 secret (CONFIRM 행)';

ALTER TABLE refund
    MODIFY COLUMN payment_id BIGINT NULL COMMENT 'FK → payment.id (CANCEL 행). RETURN은 어드민 승인 전까지 NULL',
    MODIFY COLUMN cancel_transaction_key VARCHAR(255) NULL COMMENT '환불 완료 시 Toss 취소 거래 키 (연결된 payment.last_transaction_key와 같은 값)',
    MODIFY COLUMN carrier VARCHAR(255) NULL COMMENT 'RETURN: 고객이 반품 발송한 택배사 (배송 송장은 delivery 테이블)',
    MODIFY COLUMN tracking_number VARCHAR(255) NULL COMMENT 'RETURN: 고객이 반품 발송한 송장번호';
