CREATE TABLE product_size
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at DATETIME(6)  NULL,
    updated_at DATETIME(6)  NULL,
    product_id BIGINT       NULL,
    size       VARCHAR(255) NULL COMMENT '판매 사이즈 (FREE, XS, S, M, L, XL, XXL)',
    is_deleted BIT          NOT NULL,
    CONSTRAINT fk_product_size_product FOREIGN KEY (product_id) REFERENCES product (id)
);

ALTER TABLE cart_item
    ADD COLUMN size VARCHAR(255) NULL COMMENT '선택한 사이즈. 사이즈 옵션이 없는 상품은 NULL';

ALTER TABLE order_item
    ADD COLUMN size VARCHAR(255) NULL COMMENT '주문한 사이즈. 사이즈 옵션이 없는 상품은 NULL';
