<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-04-18 | Updated: 2026-04-18 -->

# Mavis-Domain

## Purpose
비즈니스 도메인의 핵심 레이어. JPA 엔티티, Spring Data JPA 리포지토리, QueryDSL 커스텀 쿼리,
도메인 예외 코드, 비즈니스 헬퍼(implement) 클래스를 포함한다.

## Subdirectories

| Directory | Purpose |
|-----------|---------|
| `admin/` | 어드민 계정 도메인 |
| `user/` | 사용자 엔티티 (배송지, SNS 연동, 비밀번호 재설정, 인증코드 포함) |
| `product/` | 상품, 옵션, 이미지, 색상, 공지 엔티티 + QueryDSL 조회 |
| `order/` | 주문, 주문상품, 결제, 결제 멱등성, 주소 엔티티 |
| `cart/` | 장바구니 아이템 엔티티 |
| `review/` | 리뷰, 리뷰 이미지 엔티티 |
| `refund/` | 환불 엔티티 및 상태 enum |
| `delivery/` | 배송 엔티티 및 상태 enum |
| `inquiry/` | 문의, 답변, 문의 이미지 엔티티 |
| `favorite/` | 즐겨찾기 엔티티 |
| `notice/` | 공지사항 엔티티 |

## Key Domain: Order & Payment

| File | Description |
|------|-------------|
| `order/domain/Order.java` | 주문 집합 루트 엔티티 |
| `order/domain/Payment.java` | 결제 정보 엔티티 |
| `order/domain/PaymentIdempotency.java` | 결제 멱등성 관리 엔티티 |
| `order/domain/IdempotencyStatus.java` | 멱등성 상태 enum |
| `order/implement/PaymentIdempotencyManager.java` | 멱등키 생성/검증 로직 |

## For AI Agents

### Working In This Directory
- 엔티티 필드 추가/변경 시 DB 마이그레이션 스크립트 작성 필요 (Flyway/Liquibase 미사용 시 DBA 협의)
- QueryDSL 쿼리는 `*RepositoryCustom` 인터페이스 + `*RepositoryImpl` 구현 패턴 사용
- `BaseEntity`(createdAt, updatedAt) 상속 확인 후 엔티티 작성
- 도메인 예외는 각 도메인 `exception/` 패키지에 `ErrorCode` enum으로 정의

### Testing Requirements
- `./gradlew :Mavis-Domain:test`
- 결제 멱등성 테스트: `PaymentIdempotencyManagerTest`
- 주문 도메인 테스트: Order 관련 테스트

### Common Patterns
- 엔티티 구조: `domain/` (엔티티) + `repository/` (JPA + QueryDSL) + `exception/` + `implement/` (선택)
- 값 객체(VO): `ProductDisplayVO`, `ReviewAverageVO` 등 읽기 전용 프로젝션
- 커스텀 리포지토리: `*RepositoryCustom` 인터페이스 + `*RepositoryImpl` 구현

## Dependencies

### Internal
- `Mavis-Common` - 공통 예외, 상수

### External
- Spring Data JPA
- QueryDSL
- Hibernate

<!-- MANUAL: -->
