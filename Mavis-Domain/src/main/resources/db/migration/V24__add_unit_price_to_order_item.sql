ALTER TABLE order_item ADD COLUMN unit_price INT NOT NULL DEFAULT 0;
UPDATE order_item SET unit_price = total_price / quantity WHERE quantity > 0;
