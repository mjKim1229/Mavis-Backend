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

택배사가 수거하므로 고객이 택배사·송장을 입력하지 않는다. 신청은 한 주문의 여러 상품을 묶어서 할 수 있다.

### 1단계 — 사용자 반품 신청
**진입점:** `RefundService.createReturnRefund` (`POST /v1/api/refund/return`, multipart: `request` + `images`)

- Toss API: **없음**
- 요청: `orderItemIds`(같은 주문의 상품 목록), `reasonType`(`CHANGE_OF_MIND` 단순 변심 / `SELLER_FAULT` 판매자 귀책), `refundReason`(자유 텍스트)
- 검증: 소유자 확인 → 모든 상품이 같은 주문 → 배송 `DELIVERED` 확인 → 상품 중 하나라도 클레임 이력이 있으면 차단(상태 무관)

| 테이블 | 건수 | 주요 값 |
|--------|------|---------|
| `claim` | 1건 INSERT | `RETURN`, `REQUESTED`, `reason_type`, `fault_party` = null |
| `claim_item` | 선택한 상품 수만큼 INSERT | |
| `claim_image` | N건 INSERT | 반품 사진 |

`claim_return`(택배사·송장)은 더 이상 쓰지 않는다. 기존 데이터는 그대로 두며 어드민 목록에서 값이 있으면 내려준다.

### 2단계 — 어드민 승인
**진입점:** `AdminRefundFacade.approveRefund` → `AdminRefundService.validateForApproval` / `approveAndComplete` (`POST /v1/api/refund/{claimId}/approve`, body 선택)

승인 즉시 토스 부분 취소가 나간다(수거 완료 확인 단계 없음 — 상품 받은 뒤 승인하는 운영 전제).

**요청 body** (`decision`, `amount`)

| 클레임 `reasonType` | body | 처리 |
|------|------|------|
| `CHANGE_OF_MIND` | 없음 (`NOT_SELLER_FAULT`만 허용) | 배송비 차감 환불 |
| `SELLER_FAULT` | `decision` 필수 | 아래 표 |

| `decision` | 환불 총액 | `refund.shipping_fee_refund` | `claim.fault_party` |
|------|------|------|------|
| `FULL_REFUND` 전액 | 상품 금액 + 주문 배송비 | 주문 배송비 | `SELLER` |
| `PARTIAL_REFUND` 일부 | `amount` (어드민 직접 입력, 1원 이상) | 0 | `SELLER` |
| `NOT_SELLER_FAULT` 귀책 아님 | 변심과 동일 (배송비 차감) | 0 | `BUYER` |

**배송비 규칙** (`Claim.planReturnRefund`)
- 변심 환불액 = 상품 금액 합 − 주문 배송비 − 반품 배송비(`Claim.RETURN_SHIPPING_FEE` = 4,000원)
- 주문 배송비는 **주문당 1회만** 정산한다. 같은 주문에 이미 `COMPLETED`된 반품 클레임이 있으면 이후 클레임은 주문 배송비를 차감하지도, 전액 환불에 포함하지도 않는다(첫 반품이 판매자 귀책이라 돌려줬어도 동일). 거절된 클레임은 정산으로 보지 않는다.
- 반품 배송비는 클레임(수거 1회)마다 차감한다.
- 일부 환불 상한 = 토스 결제금액(`payment CONFIRM.total_amount`) − 그 주문의 `CANCEL` 환불 합. 환불액은 1원 이상 상한 이하여야 하며 벗어나면 `REFUND_400_2`. 결정이 사유와 안 맞으면 `REFUND_400_3`.
- `Claim.RETURN_SHIPPING_FEE`가 나중에 바뀌어도 과거 환불의 차감 내역은 `refund`의 스냅샷 컬럼으로 남는다.
- 일부 환불은 상품/배송비 분리를 알 수 없어 `shipping_fee_refund` = 0, 전부 `product_amount`로 기록한다.

| 테이블 | 건수 | 주요 값 |
|--------|------|---------|
| `payment` (CANCEL) | 1건 INSERT | `total_amount` = `cancel_amount` = 환불 총액 |
| `refund` | 1건 INSERT | `total_amount` = 토스 취소금액, `shipping_fee_refund` = 위 표, `order_shipping_fee_deducted` / `return_shipping_fee_deducted` = 이번 환불에서 차감한 배송비(변심·귀책 아님만 값, 그 외 0. 당시 금액 스냅샷) |
| `claim` | UPDATE | `COMPLETED`, `fault_party` = 위 표, `completed_at` |

### 2단계 — 어드민 거절
**진입점:** `AdminRefundService.rejectRefund` (`POST /v1/api/refund/{claimId}/reject`)

- Toss API: **없음**

| 테이블 | 건수 | 주요 값 |
|--------|------|---------|
| `claim` | UPDATE | `REJECTED`, `completed_at` (`refund` 행 없음) |

### 어드민 반품 목록 금액 필드
- `refundInfo.refundAmount`: 처리 전에는 배송비 차감 시 환불 예정액(0 미만이면 0), 처리 후에는 실제 환불액
- `refundInfo.fullRefundAmount`: 전액 환불 시 금액 (상품 금액 + 아직 정산 안 된 주문 배송비)
- `refundInfo.reasonType`: 고객이 선택한 사유 유형
- `orderInfo.orderItems`: 묶음 신청한 상품 전체. 기존 `orderItemId`/`orderItemInfo`는 첫 상품 값으로 유지

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
