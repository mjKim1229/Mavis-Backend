ALTER TABLE verification_code
    ADD COLUMN failed_attempts INT NOT NULL DEFAULT 0 COMMENT '현재 인증번호로 틀린 횟수. 5회가 되면 인증번호를 다시 받아야 함',
    ADD COLUMN send_count INT NOT NULL DEFAULT 0 COMMENT '발송 횟수를 세기 시작한 뒤 보낸 횟수. 10회가 되면 더 보낼 수 없음',
    ADD COLUMN send_window_started_at DATETIME(6) NULL COMMENT '발송 횟수를 세기 시작한 시각. 이 시각부터 24시간이 지나면 발송 횟수를 0으로 초기화',
    ADD COLUMN last_sent_at DATETIME(6) NULL COMMENT '마지막 발송 시각. 1분 안에는 다시 보낼 수 없음';

UPDATE verification_code SET send_window_started_at = NOW(6) WHERE send_window_started_at IS NULL;
