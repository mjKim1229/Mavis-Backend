ALTER TABLE refund MODIFY COLUMN refund_amount INT NOT NULL COMMENT '배송비 제외 순수 상품 환불금액 (OrderItem.total_price 기준)';
