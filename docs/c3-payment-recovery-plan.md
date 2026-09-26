# C3 — 토스 호출 성공 후 DB 처리 실패 시 복구 계획 (미구현)

작성일: 2026-09-20 · 상태: 계획만 수립, 코드 없음

## 문제

토스 API는 성공했는데 우리 DB 후처리가 실패하면 복구 경로가 없다.

| 흐름 | 위치 | 현재 동작 |
|---|---|---|
| 주문 취소 | `OrderFacade.cancelPayments` | 환불 완료 → `processCancelSuccess` 실패 → 멱등키 `FAILURE` → 재시도 시 `PreviousPaymentFailedException`으로 차단 |
| 환불 승인 | `AdminRefundFacade.approveRefund` | 위와 동일. Refund는 `REQUESTED`로 남고 돈만 나감 |
| 결제 승인 | `OrderFacade.confirmPayments` | 후처리 실패 시 토스 자동취소 시도. 자동취소마저 실패하면 원래 예외가 사라지고 결제 상태로 방치 |

보정 스케줄러(`PaymentReconciliationScheduler`)는 현재 주석 처리되어 동작하지 않는다.

## 핵심 원인

`IdempotencyStatus`가 `PROCESSING / SUCCESS / FAILURE` 3가지뿐이라
"외부 호출 실패(재시도 가능)"와 "외부 호출 성공 + 내부 실패(재시도 불가, 사람 개입 필요)"를 구분하지 못한다.

## 계획

### 1. 상태 구분
`IdempotencyStatus`에 `NEEDS_RECONCILE` 추가.
- 외부 호출 실패 → `FAILURE` (기존과 동일)
- 외부 호출 성공 + 후처리 실패 → `NEEDS_RECONCILE` + 응답 원문(paymentKey, transactionKey, cancelAmount) 저장
- Flyway 마이그레이션 필요: `payment_idempotency`에 응답 원문 컬럼(JSON 또는 TEXT)

### 2. 즉시 알림
`NEEDS_RECONCILE` 기록 시 Discord 알림(`DiscordNotificationService`) 발송 — 돈이 움직였는데 DB가 어긋난 상태이므로 운영자가 바로 알아야 한다.

### 3. 복구 수단
둘 중 택일 (결정 필요):
- **A. 어드민 수동 복구 API** — `NEEDS_RECONCILE` 목록 조회 + 저장된 응답 원문으로 후처리 재실행
- **B. 보정 스케줄러 재활성** — 주기적으로 `NEEDS_RECONCILE`을 토스 조회 API로 대조 후 후처리 재실행

A가 범위가 작고 예측 가능. B는 스케줄러 전반 점검이 선행되어야 함(C2 상태 전이 가드와의 충돌 확인 포함).

### 4. 결제 승인 경로
자동취소 실패 시 원래 예외를 삼키지 않도록 수정(`addSuppressed`), 자동취소 실패 자체도 `NEEDS_RECONCILE`로 기록.

## 선행 결정 사항
1. 복구 수단 A/B 중 선택
2. 응답 원문 저장 방식 — 컬럼 추가 vs 별도 테이블
3. 보정 스케줄러를 다시 켤지 여부 (C2 전이 가드 적용 후 재검증 필요)

## 참고
- C2에서 `ORDERED`/`CANCELED`는 종료 상태가 되어 어떤 전이도 불가능하다. 스케줄러를 켤 때 `order.cancel()` 호출부가 이 규칙에 걸릴 수 있다.
