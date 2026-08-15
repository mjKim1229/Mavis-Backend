ALTER TABLE orders ADD COLUMN delivery_fee int NULL;
UPDATE orders SET delivery_fee = 4000 WHERE delivery_fee IS NULL;
