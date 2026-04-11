package com.mavis.domain.domains.order.implement;

import com.mavis.domain.domains.order.domain.IdempotencyStatus;
import com.mavis.domain.domains.order.domain.PaymentApiType;
import com.mavis.domain.domains.order.domain.PaymentIdempotency;
import com.mavis.domain.domains.order.exception.PaymentAlreadyProcessingException;
import com.mavis.domain.domains.order.exception.PreviousPaymentFailedException;
import com.mavis.domain.domains.order.repository.PaymentIdempotencyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PaymentIdempotencyManagerTest {

    @InjectMocks
    private PaymentIdempotencyManager paymentIdempotencyManager;

    @Mock
    private PaymentIdempotencyRepository paymentIdempotencyRepository;

    @Test
    void 동일한_키로_중복_요청_시_기존_상태가_PROCESSING이면_PaymentAlreadyProcessingException을_던진다() {
        // given
        String idempotencyKey = "test-key-001";
        PaymentApiType apiType = PaymentApiType.CONFIRM;

        PaymentIdempotency processing = PaymentIdempotency.ofProcessing(
                idempotencyKey, apiType, LocalDateTime.now().plusDays(15)
        );

        given(paymentIdempotencyRepository.save(any())).willThrow(new DataIntegrityViolationException("unique constraint"));
        given(paymentIdempotencyRepository.findByIdempotencyKeyAndApiType(idempotencyKey, apiType))
                .willReturn(Optional.of(processing));

        // when & then
        assertThatThrownBy(() -> paymentIdempotencyManager.startProcessing(idempotencyKey, apiType))
                .isInstanceOf(PaymentAlreadyProcessingException.class);
    }

    @Test
    void 동일한_키로_중복_요청_시_기존_상태가_FAILURE이면_PreviousPaymentFailedException을_던진다() {
        // given
        String idempotencyKey = "test-key-001";
        PaymentApiType apiType = PaymentApiType.CONFIRM;

        PaymentIdempotency failed = PaymentIdempotency.ofProcessing(
                idempotencyKey, apiType, LocalDateTime.now().plusDays(15)
        );
        failed.fail("결제 승인 API 호출 실패");

        given(paymentIdempotencyRepository.save(any())).willThrow(new DataIntegrityViolationException("unique constraint"));
        given(paymentIdempotencyRepository.findByIdempotencyKeyAndApiType(idempotencyKey, apiType))
                .willReturn(Optional.of(failed));

        // when & then
        assertThatThrownBy(() -> paymentIdempotencyManager.startProcessing(idempotencyKey, apiType))
                .isInstanceOf(PreviousPaymentFailedException.class);
    }

    @Test
    void 동일한_키로_중복_요청_시_기존_상태가_SUCCESS이면_기존_객체를_반환한다() {
        // given
        String idempotencyKey = "test-key-001";
        PaymentApiType apiType = PaymentApiType.CONFIRM;

        PaymentIdempotency succeeded = PaymentIdempotency.ofProcessing(
                idempotencyKey, apiType, LocalDateTime.now().plusDays(15)
        );
        succeeded.success();

        given(paymentIdempotencyRepository.save(any())).willThrow(new DataIntegrityViolationException("unique constraint"));
        given(paymentIdempotencyRepository.findByIdempotencyKeyAndApiType(idempotencyKey, apiType))
                .willReturn(Optional.of(succeeded));

        // when
        PaymentIdempotency result = paymentIdempotencyManager.startProcessing(idempotencyKey, apiType);

        // then
        assertThat(result.getStatus()).isEqualTo(IdempotencyStatus.SUCCESS);
        assertThat(result.getIdempotencyKey()).isEqualTo(idempotencyKey);
    }
}
