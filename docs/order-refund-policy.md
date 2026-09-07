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

### RefundType
```
CANCEL → 주문 취소 환불    허용 조건: OrderStatus.PAYMENT_CONFIRMED 또는 WAITING_FOR_DEPOSIT
RETURN → 반품 신청 환불    허용 조건: DeliveryStatus.DELIVERED 이후
```

### RefundStatus
```
REQUESTED → 환불 요청 (회원 신청)
REJECTED  → 환불 거절
COMPLETED → 환불 완료 (Toss 취소 API 성공)
```

---

## 시나리오별 흐름

### 1. 일반 결제 전체 취소 (회원 API)
```
OrderStatus: PAYMENT_CONFIRMED → CANCELED
PaymentType: CONFIRM(기존) + CANCEL(신규 INSERT)
RefundType:  CANCEL
RefundStatus: COMPLETED (바로 완료)

OrderItem 수만큼 Refund 생성, 모두 동일 CANCEL Payment 참조
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

### 3. 어드민 부분 환불 (OrderItem 단위)
```
RefundStatus: REQUESTED → APPROVED → COMPLETED
PaymentType: CANCEL (부분 금액: refund.refundAmount 기준)
RefundType:  REFUND (부분 환불)

OrderItem ↔ Refund = OneToOne → 동일 OrderItem 중복 환불 불가
```
- 관련 코드: `AdminRefundService.approveAndComplete()`
- **전체 환불 완료 감지**: 모든 OrderItem의 Refund가 COMPLETED이면 Order → CANCELED

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
  - 부분 취소 = `refund.refundAmount`

---

## 엔티티 관계
```
Order (1) ──── Payment (N)       FK: payment.order_id
                   │
                   └── Refund (N) FK: refund.payment_id
                                  CANCEL Payment에만 연결

OrderItem (1) ── Refund (1)      OneToOne, 중복 환불 방지
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
| `Mavis-Domain/.../refund/domain/RefundType.java` | 환불 유형 enum |
| `Mavis-Domain/.../refund/domain/RefundStatus.java` | 환불 상태 enum |
