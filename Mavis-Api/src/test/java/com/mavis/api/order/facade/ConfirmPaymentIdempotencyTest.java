package com.mavis.api.order.facade;

import com.mavis.api.order.dto.CancelOrderRequest;
import com.mavis.api.order.dto.ConfirmPaymentResponse;
import com.mavis.api.support.ControllerTestSupport;
import com.mavis.domain.domains.order.domain.IdempotencyStatus;
import com.mavis.domain.domains.order.domain.PaymentApiType;
import com.mavis.domain.domains.order.domain.PaymentIdempotency;
import com.mavis.domain.domains.order.exception.OrderNotFoundException;
import com.mavis.domain.domains.order.exception.PaymentAlreadyProcessingException;
import com.mavis.domain.domains.order.repository.PaymentIdempotencyRepository;
import com.mavis.domain.domains.user.domain.SnsType;
import com.mavis.domain.domains.user.domain.User;
import com.mavis.domain.domains.user.repository.UserRepository;
import com.mavis.infrastructure.outer.api.tosspayments.client.PaymentsCancelClient;
import com.mavis.infrastructure.outer.api.tosspayments.client.PaymentsConfirmClient;
import com.mavis.infrastructure.outer.api.tosspayments.dto.ConfirmPaymentRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.then;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
class ConfirmPaymentIdempotencyTest extends ControllerTestSupport {

    @MockitoBean private PaymentsConfirmClient paymentsConfirmClient;
    @MockitoBean private PaymentsCancelClient paymentsCancelClient;

    @Autowired private OrderFacade orderFacade;
    @Autowired private PaymentIdempotencyRepository paymentIdempotencyRepository;
    @Autowired private UserRepository userRepository;

    @Test
    void 이미_성공한_멱등키로_결제승인을_재요청하면_토스를_호출하지_않고_건너뛴다() {
        String idempotencyKey = newKey();
        PaymentIdempotency succeeded = PaymentIdempotency.ofProcessing(
                idempotencyKey, PaymentApiType.CONFIRM, LocalDateTime.now().plusDays(15));
        succeeded.success();
        paymentIdempotencyRepository.save(succeeded);
        try {
            ConfirmPaymentResponse response = orderFacade.confirmPayments(
                    idempotencyKey, null, new ConfirmPaymentRequest("pk", "ANY-ORDER", 10000));

            assertThat(response).isNull();
            then(paymentsConfirmClient).shouldHaveNoInteractions();
        } finally {
            deleteIdempotency(idempotencyKey, PaymentApiType.CONFIRM);
        }
    }

    @Test
    void 처리중인_멱등키로_결제승인을_재요청하면_처리중_예외이고_토스를_호출하지_않는다() {
        String idempotencyKey = newKey();
        PaymentIdempotency processing = PaymentIdempotency.ofProcessing(
                idempotencyKey, PaymentApiType.CONFIRM, LocalDateTime.now().plusDays(15));
        paymentIdempotencyRepository.save(processing);
        try {
            assertThatThrownBy(() -> orderFacade.confirmPayments(
                    idempotencyKey, null, new ConfirmPaymentRequest("pk", "ANY-ORDER", 10000)))
                    .isInstanceOf(PaymentAlreadyProcessingException.class);

            then(paymentsConfirmClient).shouldHaveNoInteractions();
        } finally {
            deleteIdempotency(idempotencyKey, PaymentApiType.CONFIRM);
        }
    }

    @Test
    void 결제승인_검증이_실패하면_멱등키가_DB에_FAILURE로_남는다() {
        String idempotencyKey = newKey();
        try {
            assertThatThrownBy(() -> orderFacade.confirmPayments(
                    idempotencyKey, null, new ConfirmPaymentRequest("pk", "NO-SUCH-ORDER", 10000)))
                    .isInstanceOf(OrderNotFoundException.class);

            assertThat(reloadStatus(idempotencyKey, PaymentApiType.CONFIRM)).isEqualTo(IdempotencyStatus.FAILURE);
            then(paymentsConfirmClient).shouldHaveNoInteractions();
        } finally {
            deleteIdempotency(idempotencyKey, PaymentApiType.CONFIRM);
        }
    }

    @Test
    void 주문취소_검증이_실패하면_멱등키가_DB에_FAILURE로_남는다() {
        String idempotencyKey = newKey();
        User user = userRepository.save(User.builder()
                .name("취소테스트")
                .snsType(SnsType.KAKAO)
                .build());
        loginAs(user);
        try {
            assertThatThrownBy(() -> orderFacade.cancelPayments(
                    idempotencyKey, null, Long.MAX_VALUE, new CancelOrderRequest("사유")))
                    .isInstanceOf(OrderNotFoundException.class);

            assertThat(reloadStatus(idempotencyKey, PaymentApiType.CANCEL)).isEqualTo(IdempotencyStatus.FAILURE);
            then(paymentsCancelClient).shouldHaveNoInteractions();
        } finally {
            SecurityContextHolder.clearContext();
            deleteIdempotency(idempotencyKey, PaymentApiType.CANCEL);
            userRepository.delete(user);
        }
    }

    private String newKey() {
        return "idempotency-flow-" + UUID.randomUUID();
    }

    private void loginAs(User user) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                user.getId().toString(), null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private IdempotencyStatus reloadStatus(String idempotencyKey, PaymentApiType apiType) {
        PaymentIdempotency reloaded = paymentIdempotencyRepository.findByIdempotencyKeyAndApiType(idempotencyKey, apiType)
                .orElseThrow();
        return reloaded.getStatus();
    }

    private void deleteIdempotency(String idempotencyKey, PaymentApiType apiType) {
        paymentIdempotencyRepository.findByIdempotencyKeyAndApiType(idempotencyKey, apiType)
                .ifPresent(paymentIdempotencyRepository::delete);
    }
}
