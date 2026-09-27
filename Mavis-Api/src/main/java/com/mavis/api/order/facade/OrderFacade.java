package com.mavis.api.order.facade;

import com.mavis.api.order.dto.CancelOrderRequest;
import com.mavis.api.order.dto.ConfirmPaymentResponse;
import com.mavis.api.order.dto.PaymentCancelInfo;
import com.mavis.api.order.service.OrderService;
import com.mavis.common.properties.TossPaymentsProperties;
import com.mavis.domain.domains.order.domain.IdempotencyStatus;
import com.mavis.domain.domains.order.domain.PaymentApiType;
import com.mavis.domain.domains.order.domain.PaymentIdempotency;
import com.mavis.domain.domains.order.domain.RefundReceiveAccount;
import com.mavis.domain.domains.order.exception.CancelEntryNotFoundException;
import com.mavis.domain.domains.order.implement.PaymentIdempotencyManager;
import com.mavis.infrastructure.outer.api.tosspayments.client.PaymentsCancelClient;
import com.mavis.infrastructure.outer.api.tosspayments.client.PaymentsConfirmClient;
import com.mavis.infrastructure.outer.api.tosspayments.dto.CancelPaymentsRequest;
import com.mavis.infrastructure.outer.api.tosspayments.dto.ConfirmPaymentRequest;
import com.mavis.infrastructure.outer.api.tosspayments.dto.PaymentsCancels;
import com.mavis.infrastructure.outer.api.tosspayments.dto.PaymentsResponse;
import com.mavis.infrastructure.outer.api.tosspayments.dto.RefundReceiveAccountRequest;
import com.mavis.infrastructure.outer.api.tosspayments.dto.TossConfirmRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderFacade {

    private final TossPaymentsProperties tossPaymentsProperties;
    private final PaymentsConfirmClient paymentsConfirmClient;
    private final PaymentsCancelClient paymentsCancelClient;
    private final OrderService orderService;
    private final PaymentIdempotencyManager paymentIdempotencyManager;

    public ConfirmPaymentResponse confirmPayments(String idempotencyKey, String testCode, ConfirmPaymentRequest request) {
        PaymentIdempotency idempotency = paymentIdempotencyManager.startProcessing(idempotencyKey, PaymentApiType.CONFIRM);
        if (idempotency.getStatus() == IdempotencyStatus.SUCCESS) {
            return null;
        }
        Long idempotencyId = idempotency.getId();
        try {
            orderService.validateAndMarkPaymentRequested(request.tossOrderId(), request.amount());
        } catch (Exception e) {
            paymentIdempotencyManager.markFailure(idempotencyId, e.getMessage());
            throw e;
        }

        String authorizationHeader = tossPaymentsProperties.getAuthorizationHeader();
        TossConfirmRequest tossConfirmRequest = TossConfirmRequest.of(request.paymentKey(), request.tossOrderId(), request.amount());
        PaymentsResponse response;
        try {
            log.info("[TOSS][CONFIRM] 요청 - {}", tossConfirmRequest);
            response = paymentsConfirmClient.confirmPayments(authorizationHeader, idempotencyKey, testCode, tossConfirmRequest);
            log.info("[TOSS][CONFIRM] 완료 - {}", response);
        } catch (Exception e) {
            log.error("[TOSS][CONFIRM] 실패 - {}", tossConfirmRequest, e);
            paymentIdempotencyManager.markFailure(idempotencyId, e.getMessage());
            throw e;
        }

        try {
            orderService.processPaymentSuccess(request.tossOrderId(), response, idempotencyId);
        } catch (Exception e) {
            // 승인은 성공했고 후처리만 실패 — 고객 돈을 먼저 돌려주고(DB 상태와 무관), 그 결과에 맞게 기록한다
            log.error("[TOSS][CONFIRM] 후처리 실패, 자동 취소 시도 - {}", response, e);
            boolean canceled = tryAutoCancel(authorizationHeader, request.paymentKey());
            recordPostProcessFailure(idempotencyId, e.getMessage(), request.paymentKey(), canceled);
            throw e;
        }

        return ConfirmPaymentResponse.from(response.method());
    }

    /**
     * 승인 후처리 실패 시 결제를 자동 취소한다. 성공 여부만 반환하고, 실패는 로그로 남긴다.
     */
    private boolean tryAutoCancel(String authorizationHeader, String paymentKey) {
        try {
            paymentsCancelClient.cancelPayments(
                    authorizationHeader,
                    UUID.randomUUID().toString(),
                    null,
                    paymentKey,
                    CancelPaymentsRequest.of("결제 실패로 인한 자동 취소")
            );
            return true;
        } catch (Exception cancelException) {
            log.error("[TOSS][CONFIRM] 자동 취소 실패 — 결제 상태로 방치됨, 수동 확인 필요 - paymentKey: {}", paymentKey, cancelException);
            return false;
        }
    }

    /**
     * 자동 취소 결과에 따라 멱등키 상태를 기록한다.
     * 취소 성공이면 돈을 돌려줬으므로 FAILURE, 실패면 결제된 채 남았으므로 NEEDS_RECONCILE.
     * 후처리 실패 원인이 DB 장애면 기록도 실패할 수 있어, 기록 실패는 로그만 남기고 삼킨다.
     */
    private void recordPostProcessFailure(Long idempotencyId, String reason, String paymentKey, boolean canceled) {
        try {
            if (canceled) {
                paymentIdempotencyManager.markFailure(idempotencyId, reason);
            } else {
                paymentIdempotencyManager.markNeedsReconcile(idempotencyId, reason, paymentKey);
            }
        } catch (Exception recordException) {
            log.error("[TOSS][CONFIRM] 실패 기록 저장 실패 - paymentKey: {}, canceled: {}", paymentKey, canceled, recordException);
        }
    }

    public void cancelPayments(String idempotencyKey, String testCode, Long orderId, CancelOrderRequest request) {
        PaymentIdempotency idempotency = paymentIdempotencyManager.startProcessing(idempotencyKey, PaymentApiType.CANCEL);
        if (idempotency.getStatus() == IdempotencyStatus.SUCCESS) {
            return;
        }

        PaymentCancelInfo info;
        try {
            info = orderService.findConfirmPaymentToCancel(orderId);
        } catch (Exception e) {
            paymentIdempotencyManager.markFailure(idempotency.getId(), e.getMessage());
            throw e;
        }

        String authorizationHeader = tossPaymentsProperties.getAuthorizationHeader();
        RefundReceiveAccount account = info.refundReceiveAccount();
        RefundReceiveAccountRequest refundReceiveAccountRequest = account != null
                ? new RefundReceiveAccountRequest(account.getRefundReceiveBankCode(), account.getRefundReceiveAccountNumber(), account.getRefundReceiveHolderName())
                : null;

        CancelPaymentsRequest cancelRequest = new CancelPaymentsRequest(request.refundReason(), null, refundReceiveAccountRequest);
        PaymentsResponse paymentsResponse;
        try {
            log.info("[TOSS][CANCEL] 요청 - {}", cancelRequest);
            paymentsResponse = paymentsCancelClient.cancelPayments(
                    authorizationHeader, idempotencyKey, testCode, info.paymentKey(), cancelRequest);
            log.info("[TOSS][CANCEL] 완료 - {}", paymentsResponse);
        } catch (Exception e) {
            log.error("[TOSS][CANCEL] 실패 - {}", cancelRequest, e);
            paymentIdempotencyManager.markFailure(idempotency.getId(), e.getMessage());
            throw e;
        }

        PaymentsCancels cancelEntry = paymentsResponse.currentCancelEntry();
        if (cancelEntry == null) throw CancelEntryNotFoundException.EXCEPTION;

        try {
            // TX2: Order+Items 재조회 + Payment(CANCEL) INSERT + Refund 생성
            orderService.processCancelSuccess(info.orderId(), paymentsResponse, cancelEntry, request.refundReason(), idempotency.getId());
        } catch (Exception e) {
            // 취소는 이미 완료되어 환불됐고 DB만 반영되지 않은 상태 — 재시도가 아니라 사람의 확인이 필요하다
            log.error("[TOSS][CANCEL] 후처리 실패 — 환불은 완료됨, 정합성 확인 필요 - {}", paymentsResponse, e);
            paymentIdempotencyManager.markNeedsReconcile(idempotency.getId(), e.getMessage(), info.paymentKey());
            throw e;
        }
    }
}
