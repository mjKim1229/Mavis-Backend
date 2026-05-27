CREATE TABLE banner_image (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    image_url  VARCHAR(500) NOT NULL,
    sort_order INT          NOT NULL,
    is_deleted TINYINT(1)   NOT NULL DEFAULT 0,
    created_at DATETIME(6),
    updated_at DATETIME(6)
);
