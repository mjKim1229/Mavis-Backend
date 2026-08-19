ALTER TABLE order_item ADD COLUMN product_name VARCHAR(255) NOT NULL DEFAULT '';
UPDATE order_item oi JOIN product p ON oi.product_id = p.id SET oi.product_name = p.name;
