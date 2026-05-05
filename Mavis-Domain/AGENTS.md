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
| `order/domain/Payment.java` | 결제 원장 엔티티 (insert-only) |
| `order/domain/PaymentIdempotency.java` | 결제 멱등성 관리 엔티티 |
| `order/domain/IdempotencyStatus.java` | 멱등성 상태 enum |
| `order/implement/PaymentIdempotencyManager.java` | 멱등키 생성/검증 로직 |

### Payment 원장 설계

**Payment는 insert-only 원장 테이블이다. 기존 행을 UPDATE하지 않는다.**

결제 이벤트마다 새 Payment 행을 INSERT하며 `PaymentType`으로 구분한다.

| PaymentType | 의미 | cancel 필드 |
|-------------|------|------------|
| `CONFIRM` | 결제 승인 | null |
| `CANCEL` | 결제 취소 (전체/부분) | `cancelAmount`, `cancelReason`, `canceledAt` 세팅 |
| `DEPOSIT` | 가상계좌 입금 확인 | null |

**엔티티 관계:**

```
Order (1) ──── Payment (N)         FK: payment.order_id → orders.id
                   │
                   └── Refund (N)  FK: refund.payment_id → payment.id
                                   환불 도메인 로직 담당, Payment 원장과 분리
```

- `Order → Payment`: 한 주문에 여러 결제 이벤트 (승인 1건 + 취소 N건)
- `Payment → Refund`: CANCEL Payment에 환불 상세 연결 (Refund는 비즈니스 로직, Payment는 Toss 원장 데이터)

### 취소/환불 시나리오

**케이스 1 — 회원 API 전체 취소** (`PaymentType.CANCEL`, `RefundType.CANCEL`)
```
Payment(CANCEL) 1건 INSERT  ← order.totalPrice, Toss cancel 응답 기준
    └── Refund N건 생성      ← OrderItem 수만큼, 모두 동일 Payment 참조
Order.status → CANCELED
```

**케이스 2 — 어드민 부분 취소** (`PaymentType.CANCEL`, `RefundType.REFUND`)
```
Refund(REQUESTED) 선신청    ← 회원이 OrderItem 단위로 환불 요청
    ↓ 어드민 승인
Payment(CANCEL) 1건 INSERT  ← refund.refundAmount 기준 (부분 금액)
    └── 해당 Refund.linkPayment() + status → COMPLETED
```

**제약:**
- `OrderItem ↔ Refund` = OneToOne → 동일 OrderItem 중복 환불 불가
- `Payment(CANCEL).totalAmount`: 전체 취소 = `order.totalPrice`, 부분 취소 = `refund.refundAmount`

## For AI Agents

### Working In This Directory
- 엔티티 필드 추가/변경 시 **반드시 Flyway 마이그레이션 SQL 먼저 작성** 후 엔티티 코드 수정
- QueryDSL 쿼리는 `*RepositoryCustom` 인터페이스 + `*RepositoryImpl` 구현 패턴 사용
- `BaseEntity`(createdAt, updatedAt) 상속 확인 후 엔티티 작성
- 도메인 예외는 각 도메인 `exception/` 패키지에 `ErrorCode` enum으로 정의

### Flyway 마이그레이션 규칙
- 파일 위치: `Mavis-Domain/src/main/resources/db/migration/`
- 네이밍: `V{version}__{description}.sql` (예: `V2__add_profile_image_to_users.sql`)
- 버전: 정수 순증 (V1 → V2 → V3 ...)
- **기존 파일 내용/이름 절대 수정 금지** — Flyway checksum 검증 실패
- 작업 순서: SQL 파일 작성 → 엔티티 수정 → 로컬 실행 검증

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
