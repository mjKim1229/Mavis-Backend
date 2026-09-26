ALTER TABLE verification_code
    ADD COLUMN is_verified BIT NOT NULL DEFAULT 0 COMMENT '인증번호 확인 완료 여부. 회원가입은 이 값이 true인 행이 있어야 진행 가능';
