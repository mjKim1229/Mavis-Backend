package com.mavis.api.order.facade;

import com.mavis.api.order.service.OrderService;
import com.mavis.common.properties.TossPaymentsProperties;
import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.domain.Payment;
import com.mavis.domain.domains.order.exception.DuplicatePaymentException;
import com.mavis.domain.domains.order.implement.OrderReader;
import com.mavis.domain.domains.order.repository.PaymentRepository;
import com.mavis.infrastructure.outer.api.tosspayments.client.PaymentsCancelClient;
import com.mavis.infrastructure.outer.api.tosspayments.client.PaymentsConfirmClient;
import com.mavis.infrastructure.outer.api.tosspayments.dto.CancelPaymentsRequest;
import com.mavis.infrastructure.outer.api.tosspayments.dto.ConfirmPaymentRequest;
import com.mavis.infrastructure.outer.api.tosspayments.dto.PaymentsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderFacade {

    private final TossPaymentsProperties tossPaymentsProperties;
    private final PaymentsConfirmClient paymentsConfirmClient;
    private final PaymentsCancelClient paymentsCancelClient;
    private final OrderService orderService;
    private final OrderReader orderReader;
    private final PaymentRepository paymentRepository;

    public void confirmPayments(ConfirmPaymentRequest request) {
        if (paymentRepository.existsByPaymentKey(request.paymentKey())) {
            throw DuplicatePaymentException.EXCEPTION;
        }

        Order order = orderService.validateOrderForPayment(request.orderId(), request.amount());

        String authorizationHeader = "Basic " + Base64.getEncoder()
                .encodeToString((tossPaymentsProperties.secretKey() + ":").getBytes(StandardCharsets.UTF_8));
        PaymentsResponse response;
        try {
            response = paymentsConfirmClient.confirmPayments(authorizationHeader, request.paymentKey(), request);
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
                    request.paymentKey(),
                    new CancelPaymentsRequest("결제 실패로 인한 자동 취소")
            );
            throw e;
        }
    }

    public void cancelPayments(Long orderId, CancelPaymentsRequest cancelPaymentsRequest) {
        Order order = orderService.findOrderToCancel(orderId);
        Payment payment = paymentRepository.findByOrder(order);

        String authorizationHeader = "Basic " + Base64.getEncoder()
                .encodeToString((tossPaymentsProperties.secretKey() + ":").getBytes(StandardCharsets.UTF_8));
        PaymentsResponse paymentsResponse = paymentsCancelClient.cancelPayments(authorizationHeader, payment.getPaymentKey(), cancelPaymentsRequest);
        log.info("주문 취소 요청에 대한 응답 : {}", paymentsResponse);
        orderService.cancelOrder(order);
    }
}
