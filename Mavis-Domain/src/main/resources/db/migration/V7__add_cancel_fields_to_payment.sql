ALTER TABLE payment
    ADD COLUMN cancel_amount  BIGINT       NULL,
    ADD COLUMN cancel_reason  VARCHAR(255) NULL,
    ADD COLUMN canceled_at    DATETIME(6)  NULL;
