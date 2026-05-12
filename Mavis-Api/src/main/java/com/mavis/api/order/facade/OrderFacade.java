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
        Long idempotencyId = orderService.validateAndMarkPaymentRequested(idempotencyKey, request.tossOrderId(), request.amount());
        if (idempotencyId == null) {
            return null;
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
            log.error("[TOSS][CONFIRM] 후처리 실패, 자동 취소 시도 - {}", response, e);
            paymentIdempotencyManager.markFailure(idempotencyId, e.getMessage());
            paymentsCancelClient.cancelPayments(
                    authorizationHeader,
                    UUID.randomUUID().toString(),
                    null,
                    request.paymentKey(),
                    CancelPaymentsRequest.of("결제 실패로 인한 자동 취소")
            );
            throw e;
        }

        return ConfirmPaymentResponse.from(response.method());
    }

    public void cancelPayments(String idempotencyKey, String testCode, Long orderId, CancelOrderRequest request) {
        PaymentIdempotency idempotency = paymentIdempotencyManager.startProcessing(idempotencyKey, PaymentApiType.CANCEL);
        if (idempotency.getStatus() == IdempotencyStatus.SUCCESS) {
            return;
        }

        // TX1 (read-only): 검증 + paymentKey 조회
        PaymentCancelInfo info = orderService.findConfirmPaymentToCancel(orderId);

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
            log.error("[TOSS][CANCEL] 후처리 실패 - {}", paymentsResponse, e);
            paymentIdempotencyManager.markFailure(idempotency.getId(), e.getMessage());
            throw e;
        }
    }
}
