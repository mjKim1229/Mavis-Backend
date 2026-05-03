-- Payment : Order = 1:N 변경 (order_id unique 제거, FK 재추가)
ALTER TABLE payment ADD CONSTRAINT FK_payment_order FOREIGN KEY (order_id) REFERENCES orders (id);

-- Refund -> Payment FK 추가
ALTER TABLE refund ADD COLUMN payment_id BIGINT NULL;
ALTER TABLE refund ADD CONSTRAINT FK_refund_payment FOREIGN KEY (payment_id) REFERENCES payment (id);
