ALTER TABLE admin ADD COLUMN email VARCHAR(255);

CREATE TABLE admin_password_reset_token
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    email      VARCHAR(255) NOT NULL,
    token      VARCHAR(255) NOT NULL,
    expired_at DATETIME     NOT NULL,
    created_at DATETIME     NOT NULL,
    updated_at DATETIME     NOT NULL
);
