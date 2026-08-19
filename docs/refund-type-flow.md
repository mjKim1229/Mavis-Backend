# RefundType별 처리 흐름

## RefundType.CANCEL — 전체 취소 (배송 전)

**진입점:** `OrderFacade` → `OrderService.processCancelSuccess`

### Toss API
- `PaymentsCancelClient.cancelPayments(paymentKey, cancelRequest)` — 전액 취소
- `cancelRequest.cancelAmount` = `order.totalPrice` (배송비 포함 전체금액)

### DB 변경
| 테이블 | 건수 | 주요 값 |
|--------|------|---------|
| `Payment(CANCEL)` | 1건 INSERT | `totalAmount` = `cancelAmount` = 전체 취소금액 |
| `Refund(CANCEL, COMPLETED)` | N건 INSERT | OrderItem 수만큼, `refundAmount` = `orderItem.totalPrice` (배송비 제외 상품금액), `payment` → 위 Payment |
| `orders.order_status` | UPDATE | `CANCELED` |

---

## RefundType.RETURN — 반품 (배송 후)

### 1단계 — 사용자 반품 신청
**진입점:** `RefundService.createReturnRefund`

- Toss API: **없음**
- 검증: 소유자 확인 → 배송 `DELIVERED` 확인 → 중복 `REQUESTED` 차단

| 테이블 | 건수 | 주요 값 |
|--------|------|---------|
| `Refund(RETURN, REQUESTED)` | 1건 INSERT | `refundAmount` = `orderItem.totalPrice` (배송비 제외 상품금액), `payment` = null |
| `RefundImage` | N건 INSERT | 반품 사진 |

### 2단계 — 어드민 승인
**진입점:** `AdminRefundFacade.approveRefund` → `AdminRefundService.approveAndComplete`

- Toss API: `PaymentsCancelClient.cancelPayments(paymentKey, cancelRequest)` — **부분 취소**
  - `cancelRequest.cancelAmount` = `refund.refundAmount`
  - 가상계좌 환불 시 `refundReceiveAccount` 포함

| 테이블 | 건수 | 주요 값 |
|--------|------|---------|
| `Payment(CANCEL)` | 1건 INSERT | `totalAmount` = `cancelAmount` = `refund.refundAmount` (부분 취소금액) |
| `Refund` | UPDATE | `linkPayment()` 연결, `status → COMPLETED`, `cancelTransactionKey`, `processedAt` |

### 2단계 — 어드민 거절
**진입점:** `AdminRefundService.rejectRefund`

- Toss API: **없음**

| 테이블 | 건수 | 주요 값 |
|--------|------|---------|
| `Refund` | UPDATE | `status → REJECTED`, `processedAt` |

---

## 금액 집계 시 주의

- `Refund.refundAmount`는 배송비 제외 상품금액만. CANCEL 전체취소 시 배송비는 Refund에 안 담음 (RETURN은 정책상 배송비 환불 없어 0이 대부분 → 의미없는 컬럼이 됨)
- 전체취소 실환불액 = `sum(Refund.refundAmount) + order.deliveryFee` (`Refund → OrderItem → Order.deliveryFee` 접근)
- 정산은 Payment 원장 기준 권장 (`Payment.totalAmount` = Toss 실취소금액)

## 제약

- `OrderItem ↔ Refund` = OneToOne → 동일 OrderItem 중복 환불 DB 레벨 차단
- `Payment`는 insert-only 원장 — 기존 행 UPDATE 없음
- RETURN 전체 완료 시 Order.status 변경 없음 — CANCELED는 배송 전 전체 취소(CANCEL)에만 해당
