ALTER TABLE orders   ADD COLUMN ordered_at   datetime NULL;
ALTER TABLE delivery ADD COLUMN shipped_at   datetime NULL;
ALTER TABLE delivery ADD COLUMN delivered_at datetime NULL;
ALTER TABLE refund   ADD COLUMN processed_at datetime NULL;
