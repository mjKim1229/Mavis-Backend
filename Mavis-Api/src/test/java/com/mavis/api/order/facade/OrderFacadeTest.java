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
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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
        given(paymentIdempotencyManager.startProcessing(any(), eq(PaymentApiType.CONFIRM)))
                .willReturn(idempotency(PaymentApiType.CONFIRM, IdempotencyStatus.SUCCESS));

        orderFacade.confirmPayments("key", null, new ConfirmPaymentRequest("pk", "ORDER-001", 10000));

        then(orderService).should(never()).validateAndMarkPaymentRequested(any(), anyInt());
        then(paymentsConfirmClient).shouldHaveNoInteractions();
        then(orderService).should(never()).processPaymentSuccess(any(), any(), any());
    }

    @Test
    void 결제승인_검증_실패시_FAILURE로_기록하고_토스를_호출하지_않는다() {
        RuntimeException validationError = new RuntimeException("금액 불일치");

        given(paymentIdempotencyManager.startProcessing(any(), eq(PaymentApiType.CONFIRM)))
                .willReturn(idempotency(PaymentApiType.CONFIRM, IdempotencyStatus.PROCESSING));
        willThrow(validationError).given(orderService).validateAndMarkPaymentRequested(any(), anyInt());

        assertThatThrownBy(() ->
                orderFacade.confirmPayments("key", null, new ConfirmPaymentRequest("pk", "ORDER-001", 10000))
        ).isSameAs(validationError);

        then(paymentIdempotencyManager).should().markFailure(eq(99L), any());
        then(paymentsConfirmClient).shouldHaveNoInteractions();
    }

    @Test
    void 결제승인_성공시_processPaymentSuccess를_호출한다() {
        PaymentsResponse response = confirmResponse();

        given(paymentIdempotencyManager.startProcessing(any(), eq(PaymentApiType.CONFIRM)))
                .willReturn(idempotency(PaymentApiType.CONFIRM, IdempotencyStatus.PROCESSING));
        given(tossPaymentsProperties.getAuthorizationHeader()).willReturn("Basic xxx");
        given(paymentsConfirmClient.confirmPayments(any(), any(), any(), any())).willReturn(response);

        orderFacade.confirmPayments("key", null, new ConfirmPaymentRequest("pk", "ORDER-001", 10000));

        then(orderService).should().processPaymentSuccess("ORDER-001", response, 99L);
    }

    @Test
    void 토스_결제승인_실패시_멱등키_실패처리하고_예외를_다시_던진다() {
        RuntimeException tossError = new RuntimeException("토스 결제 오류");

        given(paymentIdempotencyManager.startProcessing(any(), eq(PaymentApiType.CONFIRM)))
                .willReturn(idempotency(PaymentApiType.CONFIRM, IdempotencyStatus.PROCESSING));
        given(tossPaymentsProperties.getAuthorizationHeader()).willReturn("Basic xxx");
        given(paymentsConfirmClient.confirmPayments(any(), any(), any(), any())).willThrow(tossError);

        assertThatThrownBy(() ->
                orderFacade.confirmPayments("key", null, new ConfirmPaymentRequest("pk", "ORDER-001", 10000))
        ).isSameAs(tossError);

        then(paymentIdempotencyManager).should().markFailure(eq(99L), any());
        then(orderService).should(never()).processPaymentSuccess(any(), any(), any());
    }

    @Test
    void 결제승인_후처리_실패_후_자동취소가_성공하면_FAILURE로_기록한다() {
        PaymentsResponse response = confirmResponse();
        RuntimeException postError = new RuntimeException("후처리 실패");

        given(paymentIdempotencyManager.startProcessing(any(), eq(PaymentApiType.CONFIRM)))
                .willReturn(idempotency(PaymentApiType.CONFIRM, IdempotencyStatus.PROCESSING));
        given(tossPaymentsProperties.getAuthorizationHeader()).willReturn("Basic xxx");
        given(paymentsConfirmClient.confirmPayments(any(), any(), any(), any())).willReturn(response);
        willThrow(postError).given(orderService).processPaymentSuccess(any(), any(), any());

        assertThatThrownBy(() ->
                orderFacade.confirmPayments("key", null, new ConfirmPaymentRequest("pk", "ORDER-001", 10000))
        ).isSameAs(postError);

        then(paymentsCancelClient).should().cancelPayments(any(), any(), any(), eq("pk"), any());
        then(paymentIdempotencyManager).should().markFailure(eq(99L), any());
        then(paymentIdempotencyManager).should(never()).markNeedsReconcile(any(), any(), any());
    }

    @Test
    void 결제승인_후처리와_자동취소가_모두_실패하면_NEEDS_RECONCILE로_기록한다() {
        PaymentsResponse response = confirmResponse();
        RuntimeException postError = new RuntimeException("후처리 실패");
        RuntimeException cancelError = new RuntimeException("자동 취소 실패");

        given(paymentIdempotencyManager.startProcessing(any(), eq(PaymentApiType.CONFIRM)))
                .willReturn(idempotency(PaymentApiType.CONFIRM, IdempotencyStatus.PROCESSING));
        given(tossPaymentsProperties.getAuthorizationHeader()).willReturn("Basic xxx");
        given(paymentsConfirmClient.confirmPayments(any(), any(), any(), any())).willReturn(response);
        willThrow(postError).given(orderService).processPaymentSuccess(any(), any(), any());
        given(paymentsCancelClient.cancelPayments(any(), any(), any(), eq("pk"), any())).willThrow(cancelError);

        assertThatThrownBy(() ->
                orderFacade.confirmPayments("key", null, new ConfirmPaymentRequest("pk", "ORDER-001", 10000))
        ).isSameAs(postError);

        then(paymentIdempotencyManager).should().markNeedsReconcile(eq(99L), any(), eq("pk"));
        then(paymentIdempotencyManager).should(never()).markFailure(any(), any());
    }

    @Test
    void 결제승인_후처리_실패시_실패_기록이_DB장애로_안되어도_자동취소는_시도한다() {
        PaymentsResponse response = confirmResponse();
        RuntimeException postError = new RuntimeException("후처리 실패 — DB 장애");
        RuntimeException dbError = new RuntimeException("DB 장애");

        given(paymentIdempotencyManager.startProcessing(any(), eq(PaymentApiType.CONFIRM)))
                .willReturn(idempotency(PaymentApiType.CONFIRM, IdempotencyStatus.PROCESSING));
        given(tossPaymentsProperties.getAuthorizationHeader()).willReturn("Basic xxx");
        given(paymentsConfirmClient.confirmPayments(any(), any(), any(), any())).willReturn(response);
        willThrow(postError).given(orderService).processPaymentSuccess(any(), any(), any());
        lenient().doThrow(dbError).when(paymentIdempotencyManager).markNeedsReconcile(any(), any(), any());
        lenient().doThrow(dbError).when(paymentIdempotencyManager).markFailure(any(), any());

        assertThatThrownBy(() ->
                orderFacade.confirmPayments("key", null, new ConfirmPaymentRequest("pk", "ORDER-001", 10000))
        ).isSameAs(postError);

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
    void 결제취소_검증_실패시_FAILURE로_기록하고_토스를_호출하지_않는다() {
        RuntimeException validationError = new RuntimeException("취소 불가 상태");

        given(paymentIdempotencyManager.startProcessing(any(), eq(PaymentApiType.CANCEL)))
                .willReturn(idempotency(PaymentApiType.CANCEL, IdempotencyStatus.PROCESSING));
        given(orderService.findConfirmPaymentToCancel(1L)).willThrow(validationError);

        assertThatThrownBy(() ->
                orderFacade.cancelPayments("key", null, 1L, new CancelOrderRequest("환불사유"))
        ).isSameAs(validationError);

        then(paymentIdempotencyManager).should().markFailure(eq(99L), any());
        then(paymentsCancelClient).shouldHaveNoInteractions();
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
    void 결제취소_후처리_실패시_NEEDS_RECONCILE로_기록하고_예외를_다시_던진다() {
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

        then(paymentIdempotencyManager).should().markNeedsReconcile(eq(99L), any(), eq("payKey"));
        then(paymentIdempotencyManager).should(never()).markFailure(any(), any());
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
                OffsetDateTime.now(), null, null, null, null, null,
                null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null
        );
    }

    private PaymentsCancels cancelEntry(String transactionKey) {
        return new PaymentsCancels(10000, "환불사유", null, null, null, null, OffsetDateTime.now(), transactionKey);
    }

    private PaymentsResponse cancelResponse(String transactionKey, PaymentsCancels entry) {
        return new PaymentsResponse(
                null, "payKey", null, "ORDER-001", null, null, null,
                null, 10000, null, PaymentsStatus.CANCELED,
                OffsetDateTime.now(), null, null, transactionKey, null, null,
                null, null, null, List.of(entry), null, null, null, null,
                null, null, null, null, null, null, null
        );
    }

    private PaymentsResponse cancelResponseWithoutEntry() {
        return new PaymentsResponse(
                null, "payKey", null, "ORDER-001", null, null, null,
                null, 10000, null, PaymentsStatus.CANCELED,
                OffsetDateTime.now(), null, null, null, null, null,
                null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null
        );
    }
}
