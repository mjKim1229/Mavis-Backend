package com.mavis.api.order.facade;

import com.mavis.api.order.dto.CancelOrderRequest;
import com.mavis.api.order.service.OrderService;
import com.mavis.common.properties.TossPaymentsProperties;
import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.domain.Payment;
import com.mavis.domain.domains.order.domain.PaymentApiType;
import com.mavis.domain.domains.order.domain.PaymentIdempotency;
import com.mavis.domain.domains.order.exception.DuplicatePaymentException;
import com.mavis.domain.domains.order.exception.PaymentAlreadyProcessingException;
import com.mavis.domain.domains.order.exception.PreviousPaymentFailedException;
import com.mavis.domain.domains.order.implement.OrderReader;
import com.mavis.domain.domains.order.implement.PaymentIdempotencyManager;
import com.mavis.domain.domains.order.implement.PaymentReader;
import com.mavis.infrastructure.outer.api.tosspayments.client.PaymentsCancelClient;
import com.mavis.infrastructure.outer.api.tosspayments.client.PaymentsConfirmClient;
import com.mavis.infrastructure.outer.api.tosspayments.dto.CancelPaymentsRequest;
import com.mavis.infrastructure.outer.api.tosspayments.dto.ConfirmPaymentRequest;
import com.mavis.infrastructure.outer.api.tosspayments.dto.PaymentsResponse;
import com.mavis.infrastructure.outer.api.tosspayments.dto.TossConfirmRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class
OrderFacade {

    private final TossPaymentsProperties tossPaymentsProperties;
    private final PaymentsConfirmClient paymentsConfirmClient;
    private final PaymentsCancelClient paymentsCancelClient;
    private final OrderService orderService;
    private final OrderReader orderReader;
    private final PaymentReader paymentReader;
    private final PaymentIdempotencyManager paymentIdempotencyManager;

    public void confirmPayments(String idempotencyKey, String testCode, ConfirmPaymentRequest request) {
        paymentIdempotencyManager.findExisting(idempotencyKey, PaymentApiType.CONFIRM)
                .ifPresent(h -> {
                    switch (h.getStatus()) {
                        case SUCCESS -> throw DuplicatePaymentException.EXCEPTION;
                        case FAILURE -> throw PreviousPaymentFailedException.EXCEPTION;
                        case PROCESSING -> throw PaymentAlreadyProcessingException.EXCEPTION;
                    }
                });

        PaymentIdempotency idempotency = paymentIdempotencyManager.startProcessing(idempotencyKey, PaymentApiType.CONFIRM);

        Order order = orderService.validateOrderForPayment(request.tossOrderId(), request.amount());

        String authorizationHeader = tossPaymentsProperties.getAuthorizationHeader();
        TossConfirmRequest tossConfirmRequest = TossConfirmRequest.of(request.paymentKey(), request.tossOrderId(), request.amount());
        PaymentsResponse response;
        try {
            response = paymentsConfirmClient.confirmPayments(authorizationHeader, idempotencyKey, testCode, tossConfirmRequest);
        } catch (Exception e) {
            log.error("토스 결제 승인 API 호출 실패", e);
            paymentIdempotencyManager.markFailure(idempotency, e.getMessage());
            throw e;
        }

        try {
            orderService.processPaymentSuccess(order, response, idempotency);
        } catch (Exception e) {
            log.error("결제 후 처리 실패, 결제 취소 시도", e);
            paymentIdempotencyManager.markFailure(idempotency, e.getMessage());
            paymentsCancelClient.cancelPayments(
                    authorizationHeader,
                    UUID.randomUUID().toString(),
                    null,
                    request.paymentKey(),
                    CancelPaymentsRequest.of("결제 실패로 인한 자동 취소")
            );
            throw e;
        }
    }

    public void cancelPayments(String idempotencyKey, String testCode, Long orderId, CancelOrderRequest request) {
        paymentIdempotencyManager.findExisting(idempotencyKey, PaymentApiType.CANCEL)
                .ifPresent(h -> {
                    switch (h.getStatus()) {
                        case SUCCESS -> throw DuplicatePaymentException.EXCEPTION;
                        case FAILURE -> throw PreviousPaymentFailedException.EXCEPTION;
                        case PROCESSING -> throw PaymentAlreadyProcessingException.EXCEPTION;
                    }
                });

        PaymentIdempotency idempotency = paymentIdempotencyManager.startProcessing(idempotencyKey, PaymentApiType.CANCEL);

        Order order = orderService.findOrderToCancel(orderId);
        Payment payment = paymentReader.findByOrder(order);

        String authorizationHeader = tossPaymentsProperties.getAuthorizationHeader();
        PaymentsResponse paymentsResponse;
        try {
            paymentsResponse = paymentsCancelClient.cancelPayments(
                    authorizationHeader, idempotencyKey, testCode, payment.getPaymentKey(),
                    CancelPaymentsRequest.of(request.refundReason()));
            log.info("주문 취소 요청에 대한 응답 : {}", paymentsResponse);
        } catch (Exception e) {
            log.error("토스 결제 취소 API 호출 실패", e);
            paymentIdempotencyManager.markFailure(idempotency, e.getMessage());
            throw e;
        }

        try {
            String cancelTransactionKey = paymentsResponse.cancels() != null && !paymentsResponse.cancels().isEmpty()
                    ? paymentsResponse.cancels().get(0).transactionKey()
                    : null;
            orderService.processCancelSuccess(order, cancelTransactionKey, request.refundReason(), idempotency);
        } catch (Exception e) {
            log.error("주문 취소 후 처리 실패", e);
            paymentIdempotencyManager.markFailure(idempotency, e.getMessage());
            throw e;
        }
    }
}
