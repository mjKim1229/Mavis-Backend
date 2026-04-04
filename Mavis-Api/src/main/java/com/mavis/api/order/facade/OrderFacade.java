package com.mavis.api.order.facade;

import com.mavis.api.order.dto.CancelOrderRequest;
import com.mavis.api.order.service.OrderService;
import com.mavis.common.properties.TossPaymentsProperties;
import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.domain.OrderItem;
import com.mavis.domain.domains.order.domain.Payment;
import com.mavis.domain.domains.order.exception.DuplicatePaymentException;
import com.mavis.domain.domains.order.implement.OrderReader;
import com.mavis.domain.domains.order.implement.PaymentReader;
import com.mavis.domain.domains.refund.domain.Refund;
import com.mavis.domain.domains.refund.domain.RefundStatus;
import com.mavis.domain.domains.refund.domain.RefundType;
import com.mavis.domain.domains.refund.implement.RefundAppender;
import com.mavis.infrastructure.outer.api.tosspayments.client.PaymentsCancelClient;
import com.mavis.infrastructure.outer.api.tosspayments.client.PaymentsConfirmClient;
import com.mavis.infrastructure.outer.api.tosspayments.dto.CancelPaymentsRequest;
import com.mavis.infrastructure.outer.api.tosspayments.dto.ConfirmPaymentRequest;
import com.mavis.infrastructure.outer.api.tosspayments.dto.PaymentsResponse;
import com.mavis.infrastructure.outer.api.tosspayments.dto.TossConfirmRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
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
    private final RefundAppender refundAppender;

    public void confirmPayments(String idempotencyKey, String testCode, ConfirmPaymentRequest request) {
        if (paymentReader.existsByPaymentKey(request.paymentKey())) {
            throw DuplicatePaymentException.EXCEPTION;
        }

        Order order = orderService.validateOrderForPayment(request.tossOrderId(), request.amount());

        String authorizationHeader = tossPaymentsProperties.getAuthorizationHeader();
        TossConfirmRequest tossConfirmRequest = TossConfirmRequest.of(request.paymentKey(), request.tossOrderId(), request.amount());
        PaymentsResponse response;
        try {
            response = paymentsConfirmClient.confirmPayments(authorizationHeader, idempotencyKey, testCode, tossConfirmRequest);
        } catch (Exception e) {
            log.error("토스 결제 승인 API 호출 실패", e);
            throw e;
        }

        try {
            orderService.processPaymentSuccess(order, response);
        } catch (Exception e) {
            log.error("결제 후 처리 실패, 결제 취소 시도", e);
            paymentsCancelClient.cancelPayments(
                    authorizationHeader,
                    UUID.randomUUID().toString(),
                    request.paymentKey(),
                    CancelPaymentsRequest.of("결제 실패로 인한 자동 취소")
            );
            throw e;
        }
    }

    public void cancelPayments(Long orderId, CancelOrderRequest request) {
        Order order = orderService.findOrderToCancel(orderId);
        Payment payment = paymentReader.findByOrder(order);

        String authorizationHeader = tossPaymentsProperties.getAuthorizationHeader();
        PaymentsResponse paymentsResponse = paymentsCancelClient.cancelPayments(
                authorizationHeader, UUID.randomUUID().toString(), payment.getPaymentKey(),
                CancelPaymentsRequest.of(request.refundReason()));
        log.info("주문 취소 요청에 대한 응답 : {}", paymentsResponse);

        String cancelTransactionKey = paymentsResponse.cancels() != null && !paymentsResponse.cancels().isEmpty()
                ? paymentsResponse.cancels().get(0).transactionKey()
                : null;

        List<Refund> refunds = order.getOrderItems().stream()
                .map(orderItem -> Refund.builder()
                        .orderItem(orderItem)
                        .refundReason(request.refundReason())
                        .refundQuantity(orderItem.getQuantity())
                        .refundAmount(orderItem.getPrice() * orderItem.getQuantity())
                        .refundStatus(RefundStatus.COMPLETED)
                        .refundType(RefundType.CANCEL)
                        .cancelTransactionKey(cancelTransactionKey)
                        .build())
                .toList();
        refundAppender.saveAll(refunds);

        orderService.cancelOrder(order);
    }
}
