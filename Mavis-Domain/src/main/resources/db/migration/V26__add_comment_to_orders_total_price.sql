ALTER TABLE orders MODIFY COLUMN total_price INT NOT NULL COMMENT '상품금액 합계 + 배송비 포함 총 결제금액';
