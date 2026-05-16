UPDATE payment SET virtual_account_due_date = DATE_ADD(virtual_account_due_date, INTERVAL 9 HOUR) WHERE virtual_account_due_date IS NOT NULL;
