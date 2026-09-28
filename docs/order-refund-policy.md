# 주문 취소 / 환불 정책

## 상태 enum 정리

### OrderStatus
```
READY               → 주문 생성 초기 상태
PAYMENT_REQUESTED   → 결제창 진입 (일반 결제)
WAITING_FOR_DEPOSIT → 가상계좌 발급 후 입금 대기
PAYMENT_CONFIRMED   → 결제 완료
ORDERED             → 어드민 발주 처리 완료
CANCELED            → 주문 취소 (전체 취소 또는 전체 환불 완료)
```

### PaymentType (`Payment` 원장 insert-only)
```
CONFIRM → 결제 승인 (가상계좌 입금 대기 포함)
CANCEL  → 결제 취소 (전체/부분)
DEPOSIT → 가상계좌 입금 확인 (입금 완료 웹훅)
```

### ClaimType (`Claim` = 고객·운영자가 요청한 것)
```
CANCEL → 배송 전 주문 취소   허용 조건: OrderStatus.PAYMENT_CONFIRMED 또는 WAITING_FOR_DEPOSIT
RETURN → 배송 후 반품        허용 조건: DeliveryStatus.DELIVERED 이후
```

### ClaimStatus
```
REQUESTED → 신청됨 (반품만. 취소는 바로 COMPLETED로 생성)
REJECTED  → 거절
COMPLETED → 처리 완료 (Toss 취소 API 성공)
```

### FaultParty (배송비 환불 여부 기준)
```
BUYER  → 구매자 귀책 (취소, 단순변심 반품)
SELLER → 판매자 귀책 (불량·오염 반품)
NULL   → 반품 검수 전
```

`Refund` = 돈이 실제로 나간 기록. 토스 취소 1번당 1행이고, 돈이 안 나가면(거절) 행이 없다.

---

## 시나리오별 흐름

### 1. 일반 결제 전체 취소 (회원 API)
```
OrderStatus: PAYMENT_CONFIRMED → CANCELED
PaymentType: CONFIRM(기존) + CANCEL(신규 INSERT)
Claim:       CANCEL, COMPLETED, BUYER — 주문의 상품 전부를 ClaimItem으로
Refund:      1행, total = 토스 취소금액, shipping_fee_refund = order.deliveryFee
```
- 관련 코드: `OrderService.processCancelSuccess()`
- `Order.cancel()` 호출 → OrderStatus CANCELED

### 2. 가상계좌 전체 취소 (회원 API)

**2-1. 입금 후 취소** (`OrderStatus.PAYMENT_CONFIRMED` 상태에서 취소)
```
OrderStatus: PAYMENT_CONFIRMED → CANCELED
PaymentType: CANCEL (환불 계좌 정보 포함)
RefundReceiveAccount: CONFIRM Payment에서 가져옴 (bankCode, accountNumber, holderName)
```
- 관련 코드: `OrderService.findConfirmPaymentToCancel()` → `confirmPayment.getRefundReceiveAccount()`
- 가상계좌 취소 시 환불 계좌 정보를 Toss에 전달해야 함

**2-2. 입금 전 취소** (`OrderStatus.WAITING_FOR_DEPOSIT` 상태에서 취소)
```
OrderStatus: WAITING_FOR_DEPOSIT → CANCELED
PaymentType: CANCEL (RefundReceiveAccount 없이 전달, null)
```
- 관련 코드: `PaymentCancelInfo.from()` — `WAITING_FOR_DEPOSIT`면 `refundReceiveAccount`를 null로 강제
- Toss 정책: 구매자가 입금하기 전이면 일반 결제와 동일하게 취소, `refundReceiveAccount` 파라미터 불필요
- **주의**: Toss 취소 응답의 `cancelAmount`는 입금 전이어도 원래 금액 그대로 내려옴(0 아님). 실제 환급 여부는 `cancelEntry.refundableAmount`로만 확인 가능(입금 전 취소 시 0) — 단, 이 값은 현재 DB에 저장하지 않음(2026-09-07 기준 별도 저장 안 하기로 결정, 필요해지면 Payment 엔티티에 컬럼 추가 검토)

**입금 전/후 판별법**: `Payment` 원장에서 해당 Order의 `PaymentType.DEPOSIT` row 존재 여부로 구분
- `CONFIRM` + `CANCEL`만 있음 → 입금 전 취소
- `CONFIRM` + `DEPOSIT` + `CANCEL` 다 있음 → 입금 후 취소
- (`Order.orderStatus`는 취소 시 무조건 `CANCELED`로 덮여써져서 이전 상태로는 구분 불가)

### 3. 반품 (OrderItem 단위)
```
신청: Claim RETURN, REQUESTED + ClaimReturn(택배사·송장) + ClaimImage
승인: Toss 부분 취소 → Payment(CANCEL) INSERT → Refund 1행 → Claim COMPLETED, fault_party 기록
거절: Claim REJECTED (Refund 없음)

claim_item.order_item_id UNIQUE → 동일 OrderItem에 클레임 1건 (거절 후 재신청도 차단)
```
- 관련 코드: `RefundService.createReturnRefund()`, `AdminRefundService.approveAndComplete()` / `rejectRefund()`
- 반품이 모두 완료돼도 Order 상태는 바뀌지 않음

### 4. 가상계좌 입금 확인 (웹훅)
```
OrderStatus: WAITING_FOR_DEPOSIT → PAYMENT_CONFIRMED
PaymentType: DEPOSIT INSERT (method는 CONFIRM Payment에서 복사)
검증: virtualAccountSecret 일치 여부 확인
```
- 관련 코드: `OrderService.processDepositCallback()`

---

## Payment 원장 설계 원칙

- **insert-only**: 기존 행 UPDATE 없음, 이벤트마다 새 행 INSERT
- `requestedAt`: Toss response의 requestedAt (UTC) 그대로 저장
- `approvedAt`: CONFIRM 타입만 존재
- `canceledAt`: CANCEL 타입만 존재
- CANCEL Payment의 `totalAmount`:
  - 전체 취소 = `order.totalPrice`
  - 반품 = 반품 상품 금액 (= 연결된 `refund.total_amount`)

---

## 엔티티 관계
```
Order (1) ── Claim (N)            FK: claim.order_id
               ├── ClaimItem (N)  FK: claim_item.claim_id, order_item_id UNIQUE
               ├── ClaimReturn (0..1)  반품만. Claim은 참조하지 않음 → ClaimReturnRepository로 조회
               ├── ClaimImage (N)
               └── Refund (0..N)  FK: refund.claim_id, refund.payment_id → Payment(CANCEL)

Order (1) ── Payment (N)          FK: payment.order_id (토스 원장)
```

---

## 관련 파일

| 파일 | 역할 |
|------|------|
| `Mavis-Api/.../order/service/OrderService.java` | 결제 승인/취소/웹훅 처리 |
| `Mavis-Admin/.../refund/service/AdminRefundService.java` | 어드민 환불 승인/거절 |
| `Mavis-Domain/.../order/domain/Payment.java` | 결제 원장 엔티티 |
| `Mavis-Domain/.../order/domain/OrderStatus.java` | 주문 상태 enum |
| `Mavis-Domain/.../order/domain/PaymentType.java` | 결제 이벤트 타입 enum |
| `Mavis-Domain/.../claim/domain/Claim.java` | 취소·반품 요청 엔티티 (상태 전이 규칙 포함) |
| `Mavis-Domain/.../refund/domain/Refund.java` | 환불 금액 기록 (상품/배송비 분리) |
| `docs/claim-flow.md` | 취소·반품 흐름별 테이블 변경 |
