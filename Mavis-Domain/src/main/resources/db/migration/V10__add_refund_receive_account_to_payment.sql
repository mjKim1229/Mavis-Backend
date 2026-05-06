ALTER TABLE payment
    ADD COLUMN refund_receive_bank_code VARCHAR(10) NULL,
    ADD COLUMN refund_receive_account_number VARCHAR(50) NULL,
    ADD COLUMN refund_receive_holder_name VARCHAR(50) NULL;
