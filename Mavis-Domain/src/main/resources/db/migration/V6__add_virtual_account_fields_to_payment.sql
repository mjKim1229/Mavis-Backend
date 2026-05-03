ALTER TABLE payment ADD COLUMN virtual_account_number varchar(255) NULL;
ALTER TABLE payment ADD COLUMN virtual_account_bank_code varchar(255) NULL;
ALTER TABLE payment ADD COLUMN virtual_account_due_date datetime NULL;
ALTER TABLE payment ADD COLUMN virtual_account_depositor_name varchar(255) NULL;
