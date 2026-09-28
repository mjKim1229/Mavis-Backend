CREATE TABLE claim
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at       DATETIME(6)  NULL,
    updated_at       DATETIME(6)  NULL,
    order_id         BIGINT       NOT NULL COMMENT 'FK → orders.id',
    claim_type       VARCHAR(255) NOT NULL COMMENT 'CANCEL: 배송 전 주문 취소 / RETURN: 배송 후 반품',
    claim_status     VARCHAR(255) NOT NULL COMMENT 'REQUESTED: 신청됨 / COMPLETED: 처리 완료 / REJECTED: 거절',
    reason           VARCHAR(255) NULL COMMENT '고객이 입력한 사유',
    fault_party      VARCHAR(255) NULL COMMENT 'BUYER: 구매자 귀책 / SELLER: 판매자 귀책. 배송비 환불 여부 기준, 검수 전 NULL',
    completed_at     DATETIME(6)  NULL COMMENT '완료 또는 거절 처리 시각. 처리 전 NULL',
    legacy_refund_id BIGINT       NULL,
    CONSTRAINT fk_claim_order FOREIGN KEY (order_id) REFERENCES orders (id)
);

CREATE TABLE claim_item
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at    DATETIME(6) NULL,
    updated_at    DATETIME(6) NULL,
    claim_id      BIGINT      NOT NULL COMMENT 'FK → claim.id',
    order_item_id BIGINT      NOT NULL COMMENT 'FK → order_item.id. 상품당 클레임 1건 (중복 신청 차단)',
    CONSTRAINT uk_claim_item_order_item UNIQUE (order_item_id),
    CONSTRAINT fk_claim_item_claim FOREIGN KEY (claim_id) REFERENCES claim (id),
    CONSTRAINT fk_claim_item_order_item FOREIGN KEY (order_item_id) REFERENCES order_item (id)
);

CREATE TABLE claim_return
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at      DATETIME(6)  NULL,
    updated_at      DATETIME(6)  NULL,
    claim_id        BIGINT       NOT NULL COMMENT 'FK → claim.id (반품 클레임과 1:1)',
    carrier         VARCHAR(255) NOT NULL COMMENT '고객이 반품 발송한 택배사',
    tracking_number VARCHAR(255) NOT NULL COMMENT '고객이 반품 발송한 송장번호',
    CONSTRAINT uk_claim_return_claim UNIQUE (claim_id),
    CONSTRAINT fk_claim_return_claim FOREIGN KEY (claim_id) REFERENCES claim (id)
);

CREATE TABLE claim_image
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at DATETIME(6)  NULL,
    updated_at DATETIME(6)  NULL,
    claim_id   BIGINT       NOT NULL COMMENT 'FK → claim.id',
    image_url  VARCHAR(255) NOT NULL COMMENT '고객이 첨부한 사진 S3 URL',
    CONSTRAINT fk_claim_image_claim FOREIGN KEY (claim_id) REFERENCES claim (id)
);

-- 기존 refund와 FK 이름이 겹치지 않도록 FK는 테이블 교체 후에 건다
CREATE TABLE refund_new
(
    id                     BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at             DATETIME(6)  NULL,
    updated_at             DATETIME(6)  NULL,
    claim_id               BIGINT       NOT NULL COMMENT 'FK → claim.id',
    payment_id             BIGINT       NOT NULL COMMENT 'FK → payment.id (CANCEL 원장 행)',
    total_amount           INT          NOT NULL COMMENT '이번에 돌려준 총액 = 토스 취소 금액 (product_amount + shipping_fee_refund)',
    product_amount         INT          NOT NULL COMMENT '총액 중 상품 금액',
    shipping_fee_refund    INT          NOT NULL COMMENT '총액 중 배송비. 0이면 배송비 환불 안 함',
    cancel_transaction_key VARCHAR(255) NOT NULL COMMENT 'Toss 취소 거래 키 (payment.last_transaction_key와 같은 값)'
);

INSERT INTO claim (created_at, updated_at, order_id, claim_type, claim_status, reason, fault_party, completed_at, legacy_refund_id)
SELECT MIN(r.created_at), MIN(r.updated_at), p.order_id, 'CANCEL', 'COMPLETED', MIN(r.refund_reason), 'BUYER', MIN(r.created_at), MIN(r.id)
FROM refund r
         JOIN payment p ON p.id = r.payment_id
WHERE r.refund_type = 'CANCEL'
GROUP BY p.order_id;

INSERT INTO claim_item (created_at, updated_at, claim_id, order_item_id)
SELECT r.created_at, r.updated_at, c.id, r.order_item_id
FROM refund r
         JOIN payment p ON p.id = r.payment_id
         JOIN claim c ON c.order_id = p.order_id AND c.claim_type = 'CANCEL'
WHERE r.refund_type = 'CANCEL';

INSERT INTO refund_new (created_at, updated_at, claim_id, payment_id, total_amount, product_amount, shipping_fee_refund, cancel_transaction_key)
SELECT c.created_at, c.updated_at, c.id, p.id, p.cancel_amount,
       p.cancel_amount - COALESCE(o.delivery_fee, 0), COALESCE(o.delivery_fee, 0), r.cancel_transaction_key
FROM claim c
         JOIN refund r ON r.id = c.legacy_refund_id
         JOIN payment p ON p.id = r.payment_id
         JOIN orders o ON o.id = c.order_id
WHERE c.claim_type = 'CANCEL';

INSERT INTO claim (created_at, updated_at, order_id, claim_type, claim_status, reason, fault_party, completed_at, legacy_refund_id)
SELECT r.created_at, r.updated_at, oi.order_id, 'RETURN', r.refund_status, r.refund_reason,
       CASE WHEN r.refund_status = 'COMPLETED' THEN 'BUYER' END,
       CASE WHEN r.refund_status <> 'REQUESTED' THEN COALESCE(r.processed_at, r.updated_at) END,
       r.id
FROM refund r
         JOIN order_item oi ON oi.id = r.order_item_id
WHERE r.refund_type = 'RETURN'
  AND NOT (r.refund_status = 'COMPLETED' AND r.payment_id IS NULL);

INSERT INTO claim_item (created_at, updated_at, claim_id, order_item_id)
SELECT r.created_at, r.updated_at, c.id, r.order_item_id
FROM claim c
         JOIN refund r ON r.id = c.legacy_refund_id
WHERE c.claim_type = 'RETURN';

INSERT INTO claim_return (created_at, updated_at, claim_id, carrier, tracking_number)
SELECT r.created_at, r.updated_at, c.id, r.carrier, r.tracking_number
FROM claim c
         JOIN refund r ON r.id = c.legacy_refund_id
WHERE c.claim_type = 'RETURN'
  AND TRIM(COALESCE(r.carrier, '')) <> ''
  AND TRIM(COALESCE(r.tracking_number, '')) <> '';

INSERT INTO claim_image (created_at, updated_at, claim_id, image_url)
SELECT ri.created_at, ri.updated_at, c.id, ri.image_url
FROM refund_image ri
         JOIN claim c ON c.legacy_refund_id = ri.refund_id AND c.claim_type = 'RETURN';

INSERT INTO refund_new (created_at, updated_at, claim_id, payment_id, total_amount, product_amount, shipping_fee_refund, cancel_transaction_key)
SELECT COALESCE(r.processed_at, r.updated_at), r.updated_at, c.id, r.payment_id,
       r.refund_amount, r.refund_amount, 0, r.cancel_transaction_key
FROM claim c
         JOIN refund r ON r.id = c.legacy_refund_id
WHERE c.claim_type = 'RETURN'
  AND c.claim_status = 'COMPLETED';

DROP TABLE refund_image;
DROP TABLE refund;
RENAME TABLE refund_new TO refund;

ALTER TABLE refund
    ADD CONSTRAINT fk_refund_claim FOREIGN KEY (claim_id) REFERENCES claim (id),
    ADD CONSTRAINT fk_refund_payment FOREIGN KEY (payment_id) REFERENCES payment (id);

ALTER TABLE claim DROP COLUMN legacy_refund_id;
