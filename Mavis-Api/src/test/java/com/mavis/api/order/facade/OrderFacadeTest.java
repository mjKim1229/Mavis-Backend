package com.mavis.api.order.facade;

import com.mavis.api.order.dto.CancelOrderRequest;
import com.mavis.api.order.dto.PaymentCancelInfo;
import com.mavis.api.order.service.OrderService;
import com.mavis.common.properties.TossPaymentsProperties;
import com.mavis.domain.domains.order.domain.*;
import com.mavis.domain.domains.order.exception.CancelEntryNotFoundException;
import com.mavis.domain.domains.order.implement.PaymentIdempotencyManager;
import com.mavis.infrastructure.outer.api.tosspayments.client.PaymentsCancelClient;
import com.mavis.infrastructure.outer.api.tosspayments.client.PaymentsConfirmClient;
import com.mavis.infrastructure.outer.api.tosspayments.dto.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class OrderFacadeTest {

    @InjectMocks
    private OrderFacade orderFacade;

    @Mock
    private TossPaymentsProperties tossPaymentsProperties;
    @Mock
    private PaymentsConfirmClient paymentsConfirmClient;
    @Mock
    private PaymentsCancelClient paymentsCancelClient;
    @Mock
    private OrderService orderService;
    @Mock
    private PaymentIdempotencyManager paymentIdempotencyManager;

    // ── confirmPayments ───────────────────────────────────────────────────────

    @Test
    void 멱등키가_SUCCESS면_결제승인을_건너뛴다() {
        given(orderService.validateAndMarkPaymentRequested(any(), any(), anyInt())).willReturn(null);

        orderFacade.confirmPayments("key", null, new ConfirmPaymentRequest("pk", "ORDER-001", 10000));

        then(paymentsConfirmClient).shouldHaveNoInteractions();
        then(orderService).should(never()).processPaymentSuccess(any(), any(), any());
    }

    @Test
    void 결제승인_성공시_processPaymentSuccess를_호출한다() {
        PaymentsResponse response = confirmResponse();

        given(orderService.validateAndMarkPaymentRequested(any(), any(), anyInt())).willReturn(99L);
        given(tossPaymentsProperties.getAuthorizationHeader()).willReturn("Basic xxx");
        given(paymentsConfirmClient.confirmPayments(any(), any(), any(), any())).willReturn(response);

        orderFacade.confirmPayments("key", null, new ConfirmPaymentRequest("pk", "ORDER-001", 10000));

        then(orderService).should().processPaymentSuccess("ORDER-001", response, 99L);
    }

    @Test
    void 토스_결제승인_실패시_멱등키_실패처리하고_예외를_다시_던진다() {
        RuntimeException tossError = new RuntimeException("토스 결제 오류");

        given(orderService.validateAndMarkPaymentRequested(any(), any(), anyInt())).willReturn(99L);
        given(tossPaymentsProperties.getAuthorizationHeader()).willReturn("Basic xxx");
        given(paymentsConfirmClient.confirmPayments(any(), any(), any(), any())).willThrow(tossError);

        assertThatThrownBy(() ->
                orderFacade.confirmPayments("key", null, new ConfirmPaymentRequest("pk", "ORDER-001", 10000))
        ).isSameAs(tossError);

        then(paymentIdempotencyManager).should().markFailure(eq(99L), any());
        then(orderService).should(never()).processPaymentSuccess(any(), any(), any());
    }

    @Test
    void 결제승인_후처리_실패시_자동취소_요청하고_예외를_다시_던진다() {
        PaymentsResponse response = confirmResponse();
        RuntimeException postError = new RuntimeException("후처리 실패");

        given(orderService.validateAndMarkPaymentRequested(any(), any(), anyInt())).willReturn(99L);
        given(tossPaymentsProperties.getAuthorizationHeader()).willReturn("Basic xxx");
        given(paymentsConfirmClient.confirmPayments(any(), any(), any(), any())).willReturn(response);
        willThrow(postError).given(orderService).processPaymentSuccess(any(), any(), any());

        assertThatThrownBy(() ->
                orderFacade.confirmPayments("key", null, new ConfirmPaymentRequest("pk", "ORDER-001", 10000))
        ).isSameAs(postError);

        then(paymentIdempotencyManager).should().markFailure(eq(99L), any());
        then(paymentsCancelClient).should().cancelPayments(any(), any(), any(), eq("pk"), any());
    }

    // ── cancelPayments ────────────────────────────────────────────────────────

    @Test
    void 멱등키가_SUCCESS면_결제취소를_건너뛴다() {
        given(paymentIdempotencyManager.startProcessing(any(), eq(PaymentApiType.CANCEL)))
                .willReturn(idempotency(PaymentApiType.CANCEL, IdempotencyStatus.SUCCESS));

        orderFacade.cancelPayments("key", null, 1L, new CancelOrderRequest("환불사유"));

        then(paymentsCancelClient).shouldHaveNoInteractions();
        then(orderService).shouldHaveNoInteractions();
    }

    @Test
    void 결제취소_성공시_processCancelSuccess를_호출한다() {
        PaymentIdempotency idempotency = idempotency(PaymentApiType.CANCEL, IdempotencyStatus.PROCESSING);
        PaymentCancelInfo info = new PaymentCancelInfo(1L, "payKey", null);
        PaymentsCancels cancelEntry = cancelEntry("txKey");
        PaymentsResponse response = cancelResponse("txKey", cancelEntry);

        given(paymentIdempotencyManager.startProcessing(any(), eq(PaymentApiType.CANCEL))).willReturn(idempotency);
        given(tossPaymentsProperties.getAuthorizationHeader()).willReturn("Basic xxx");
        given(orderService.findConfirmPaymentToCancel(1L)).willReturn(info);
        given(paymentsCancelClient.cancelPayments(any(), any(), any(), any(), any())).willReturn(response);

        orderFacade.cancelPayments("key", null, 1L, new CancelOrderRequest("환불사유"));

        then(orderService).should().processCancelSuccess(1L, response, cancelEntry, "환불사유", 99L);
    }

    @Test
    void 토스_결제취소_실패시_멱등키_실패처리하고_예외를_다시_던진다() {
        PaymentIdempotency idempotency = idempotency(PaymentApiType.CANCEL, IdempotencyStatus.PROCESSING);
        RuntimeException tossError = new RuntimeException("토스 취소 오류");

        given(paymentIdempotencyManager.startProcessing(any(), eq(PaymentApiType.CANCEL))).willReturn(idempotency);
        given(tossPaymentsProperties.getAuthorizationHeader()).willReturn("Basic xxx");
        given(orderService.findConfirmPaymentToCancel(1L)).willReturn(new PaymentCancelInfo(1L, "payKey", null));
        given(paymentsCancelClient.cancelPayments(any(), any(), any(), any(), any())).willThrow(tossError);

        assertThatThrownBy(() ->
                orderFacade.cancelPayments("key", null, 1L, new CancelOrderRequest("환불사유"))
        ).isSameAs(tossError);

        then(paymentIdempotencyManager).should().markFailure(eq(99L), any());
        then(orderService).should(never()).processCancelSuccess(any(), any(), any(), any(), any());
    }

    @Test
    void 취소응답에_cancelEntry가_없으면_CancelEntryNotFoundException을_던진다() {
        given(paymentIdempotencyManager.startProcessing(any(), eq(PaymentApiType.CANCEL)))
                .willReturn(idempotency(PaymentApiType.CANCEL, IdempotencyStatus.PROCESSING));
        given(tossPaymentsProperties.getAuthorizationHeader()).willReturn("Basic xxx");
        given(orderService.findConfirmPaymentToCancel(1L)).willReturn(new PaymentCancelInfo(1L, "payKey", null));
        given(paymentsCancelClient.cancelPayments(any(), any(), any(), any(), any()))
                .willReturn(cancelResponseWithoutEntry());

        assertThatThrownBy(() ->
                orderFacade.cancelPayments("key", null, 1L, new CancelOrderRequest("환불사유"))
        ).isInstanceOf(CancelEntryNotFoundException.class);
    }

    @Test
    void 결제취소_후처리_실패시_멱등키_실패처리하고_예외를_다시_던진다() {
        PaymentIdempotency idempotency = idempotency(PaymentApiType.CANCEL, IdempotencyStatus.PROCESSING);
        PaymentsCancels cancelEntry = cancelEntry("txKey");
        PaymentsResponse response = cancelResponse("txKey", cancelEntry);
        RuntimeException postError = new RuntimeException("후처리 실패");

        given(paymentIdempotencyManager.startProcessing(any(), eq(PaymentApiType.CANCEL))).willReturn(idempotency);
        given(tossPaymentsProperties.getAuthorizationHeader()).willReturn("Basic xxx");
        given(orderService.findConfirmPaymentToCancel(1L)).willReturn(new PaymentCancelInfo(1L, "payKey", null));
        given(paymentsCancelClient.cancelPayments(any(), any(), any(), any(), any())).willReturn(response);
        willThrow(postError).given(orderService).processCancelSuccess(any(), any(), any(), any(), any());

        assertThatThrownBy(() ->
                orderFacade.cancelPayments("key", null, 1L, new CancelOrderRequest("환불사유"))
        ).isSameAs(postError);

        then(paymentIdempotencyManager).should().markFailure(eq(99L), any());
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private PaymentIdempotency idempotency(PaymentApiType type, IdempotencyStatus status) {
        return PaymentIdempotency.builder()
                .id(99L)
                .idempotencyKey("key")
                .apiType(type)
                .status(status)
                .expiredAt(LocalDateTime.now().plusMinutes(10))
                .build();
    }

    private PaymentsResponse confirmResponse() {
        return new PaymentsResponse(
                null, "pk", null, "ORDER-001", null, null, null,
                null, 10000, null, PaymentsStatus.DONE,
                ZonedDateTime.now(), null, null, null, null, null,
                null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null
        );
    }

    private PaymentsCancels cancelEntry(String transactionKey) {
        return new PaymentsCancels(10000, "환불사유", null, null, null, null, ZonedDateTime.now(), transactionKey);
    }

    private PaymentsResponse cancelResponse(String transactionKey, PaymentsCancels entry) {
        return new PaymentsResponse(
                null, "payKey", null, "ORDER-001", null, null, null,
                null, 10000, null, PaymentsStatus.CANCELED,
                ZonedDateTime.now(), null, null, transactionKey, null, null,
                null, null, null, List.of(entry), null, null, null, null,
                null, null, null, null, null, null, null
        );
    }

    private PaymentsResponse cancelResponseWithoutEntry() {
        return new PaymentsResponse(
                null, "payKey", null, "ORDER-001", null, null, null,
                null, 10000, null, PaymentsStatus.CANCELED,
                ZonedDateTime.now(), null, null, null, null, null,
                null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null
        );
    }
}
