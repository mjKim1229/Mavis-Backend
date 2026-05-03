-- Payment : Order = 1:N 변경 (order_id unique 제거)
ALTER TABLE payment DROP FOREIGN KEY FKlouu98csyullos9k25tbpk4va;
ALTER TABLE payment DROP INDEX UKmf7n8wo2rwrxsd6f3t9ub2mep;
ALTER TABLE payment ADD CONSTRAINT FKlouu98csyullos9k25tbpk4va FOREIGN KEY (order_id) REFERENCES orders (id);

-- Refund -> Payment FK 추가
ALTER TABLE refund ADD COLUMN payment_id BIGINT NULL;
ALTER TABLE refund ADD CONSTRAINT FK_refund_payment FOREIGN KEY (payment_id) REFERENCES payment (id);
