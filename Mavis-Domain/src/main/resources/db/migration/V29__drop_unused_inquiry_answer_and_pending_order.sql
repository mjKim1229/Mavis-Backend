-- inquiry.answer: 엔티티 미매핑 레거시 컬럼. 답변은 inquiry_answer 테이블 사용
ALTER TABLE inquiry DROP COLUMN answer;

-- pending_order: 엔티티·코드 참조 없는 레거시 테이블 (FK 없음)
DROP TABLE IF EXISTS pending_order;
