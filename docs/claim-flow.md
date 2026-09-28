# 취소·반품 처리 흐름 (Claim / Refund)

- `Claim`: 고객이 요청한 것 (취소·반품). 상태 흐름을 가진다.
- `Refund`: 돈이 실제로 나간 기록. 토스 취소 1번당 1행, 금액은 상품/배송비로 나눠 스냅샷 저장한다. 돈이 안 나가면(거절) 행이 없다.
- `Payment`: 토스 원장 (insert-only). `Refund.payment_id`가 CANCEL 행을 가리킨다.

## ClaimType.CANCEL — 전체 취소 (배송 전)

**진입점:** `OrderFacade` → `OrderService.processCancelSuccess`

**허용 상태:** `OrderStatus.PAYMENT_CONFIRMED` 또는 `WAITING_FOR_DEPOSIT` (2026-09-07부터 가상계좌 입금 전 취소도 허용)

### Toss API
- `PaymentsCancelClient.cancelPayments(paymentKey, cancelRequest)` — 전액 취소
- `cancelRequest.cancelAmount` = null (전액)
- `refundReceiveAccount`: `PAYMENT_CONFIRMED`(입금 후)면 전달, `WAITING_FOR_DEPOSIT`(입금 전)면 null (Toss 정책)

### DB 변경
| 테이블 | 건수 | 주요 값 |
|--------|------|---------|
| `payment` (CANCEL) | 1건 INSERT | `total_amount` = `cancel_amount` = 전체 취소금액 |
| `claim` | 1건 INSERT | `CANCEL`, `COMPLETED`, `fault_party = BUYER`, `completed_at` = 처리 시각 |
| `claim_item` | 주문 상품 수만큼 INSERT | |
| `refund` | 1건 INSERT | `total_amount` = 토스 취소금액, `shipping_fee_refund` = `order.delivery_fee`, `product_amount` = 나머지 |
| `orders.order_status` | UPDATE | `CANCELED` |

### 입금 전/후 취소 구분 (실측, 2026-09-07)
- 가상계좌 입금 전 취소해도 Toss 응답 `cancels[].cancelAmount`는 원래 금액 그대로 내려옴(0 아님). 실환급 여부는 `cancels[].refundableAmount`로만 판별 가능(입금 전이면 0) — 현재 이 값은 DB 미저장.
- DB 레벨 구분법: `Payment(DEPOSIT)` row 존재 여부
  - 없음 → `CONFIRM` + `CANCEL`만 존재 → 입금 전 취소
  - 있음 → `CONFIRM` + `DEPOSIT` + `CANCEL` 전부 존재 → 입금 후 취소
- Admin 취소 조회 응답(`GetAdminCanceledRefundResponse`)엔 `paymentMethod`만 노출, 입금 전/후 구분 플래그는 추가 안 하기로 함(2026-09-07 결정 — 필요성 낮음)

---

## ClaimType.RETURN — 반품 (배송 후)

### 1단계 — 사용자 반품 신청
**진입점:** `RefundService.createReturnRefund` (`POST /v1/api/refund/{orderItemId}/return`)

- Toss API: **없음**
- 검증: 소유자 확인 → 배송 `DELIVERED` 확인 → 같은 상품에 클레임 이력이 있으면 차단(상태 무관) → 택배사·송장 필수(`@NotBlank`)

| 테이블 | 건수 | 주요 값 |
|--------|------|---------|
| `claim` | 1건 INSERT | `RETURN`, `REQUESTED`, `fault_party` = null |
| `claim_item` | 1건 INSERT | 반품 상품 (신청은 상품 단위) |
| `claim_return` | 1건 INSERT | 고객이 보낸 택배사·송장 |
| `claim_image` | N건 INSERT | 반품 사진 |

### 2단계 — 어드민 승인
**진입점:** `AdminRefundFacade.approveRefund` → `AdminRefundService.approveAndComplete` (`POST /v1/api/refund/{claimId}/approve`)

- Toss API: `PaymentsCancelClient.cancelPayments(paymentKey, cancelRequest)` — **부분 취소**
  - `cancelRequest.cancelAmount` = 클레임 상품 금액 합
  - 가상계좌 환불 시 `refundReceiveAccount` 포함

| 테이블 | 건수 | 주요 값 |
|--------|------|---------|
| `payment` (CANCEL) | 1건 INSERT | `total_amount` = `cancel_amount` = 부분 취소금액 |
| `refund` | 1건 INSERT | `total_amount` = 토스 취소금액, `shipping_fee_refund` = 0 |
| `claim` | UPDATE | `COMPLETED`, `fault_party = BUYER`, `completed_at` |

### 2단계 — 어드민 거절
**진입점:** `AdminRefundService.rejectRefund` (`POST /v1/api/refund/{claimId}/reject`)

- Toss API: **없음**

| 테이블 | 건수 | 주요 값 |
|--------|------|---------|
| `claim` | UPDATE | `REJECTED`, `completed_at` (`refund` 행 없음) |

---

## 금액 집계

- 실제 환불액은 `refund`만 보면 된다: `total_amount = product_amount + shipping_fee_refund`
- 주문별 배송비 환불 합: `refund JOIN claim ON claim.order_id` 의 `shipping_fee_refund` 합
- `refund.total_amount` = 연결된 `payment.cancel_amount` (토스 실취소금액)

## 제약

- `claim_item.order_item_id` UNIQUE → 동일 OrderItem에 클레임 1건
- `Claim` 상태 전이는 엔티티가 검증: 반품 `REQUESTED`에서만 `complete`/`reject` 가능, 취소 클레임은 생성 즉시 완료
- `Payment`는 insert-only 원장 — 기존 행 UPDATE 없음
- 반품이 모두 완료돼도 `Order.status` 변경 없음 — `CANCELED`는 배송 전 전체 취소에만 해당

## API 호환 (FE)

- 어드민 반품 목록 `refundInfo.refundId`, 취소 목록 `cancelInfo.refundId`는 필드명을 유지했다. 값은 각각 `claim.id`, `claim_item.id`(취소 목록은 상품 단위 행이라 행마다 고유한 값)
- 사용자 주문 목록 `orderProductList[].refundStatus` 는 반품 클레임 상태만 담는다 (취소 주문의 상품은 null)
