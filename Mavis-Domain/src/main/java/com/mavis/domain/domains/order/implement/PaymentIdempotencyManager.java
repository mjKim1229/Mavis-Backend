package com.mavis.domain.domains.order.implement;

import com.mavis.domain.domains.order.domain.PaymentApiType;
import com.mavis.domain.domains.order.domain.PaymentIdempotency;
import com.mavis.domain.domains.order.exception.IdempotencyNotFoundException;
import com.mavis.domain.domains.order.exception.PaymentAlreadyProcessingException;
import com.mavis.domain.domains.order.exception.PaymentNeedsReconcileException;
import com.mavis.domain.domains.order.exception.PreviousPaymentFailedException;
import com.mavis.domain.domains.order.repository.PaymentIdempotencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class PaymentIdempotencyManager {

    private static final int EXPIRY_DAYS = 15;

    private final PaymentIdempotencyRepository paymentIdempotencyRepository;

    public PaymentIdempotency startProcessing(String idempotencyKey, PaymentApiType apiType) {
        try {
            return paymentIdempotencyRepository.save(
                    PaymentIdempotency.ofProcessing(idempotencyKey, apiType, LocalDateTime.now().plusDays(EXPIRY_DAYS))
            );
        } catch (DataIntegrityViolationException e) {
            PaymentIdempotency existing = paymentIdempotencyRepository
                    .findByIdempotencyKeyAndApiType(idempotencyKey, apiType)
                    .orElseThrow(() -> IdempotencyNotFoundException.EXCEPTION);

            switch (existing.getStatus()) {
                case PROCESSING -> throw PaymentAlreadyProcessingException.EXCEPTION;
                case FAILURE -> throw PreviousPaymentFailedException.EXCEPTION;
                // 외부 호출은 이미 성공한 건 — 재시도하면 중복 결제/취소가 되므로 막고 고객센터로 안내한다
                case NEEDS_RECONCILE -> throw PaymentNeedsReconcileException.EXCEPTION;
                case SUCCESS -> { return existing; }
            }
            throw IdempotencyNotFoundException.EXCEPTION;
        }
    }

    @Transactional
    public void markSuccess(Long idempotencyId) {
        PaymentIdempotency idempotency = paymentIdempotencyRepository.findById(idempotencyId)
                .orElseThrow(() -> IdempotencyNotFoundException.EXCEPTION);
        idempotency.success();
    }

    @Transactional
    public void markFailure(Long idempotencyId, String reason) {
        PaymentIdempotency idempotency = paymentIdempotencyRepository.findById(idempotencyId)
                .orElseThrow(() -> IdempotencyNotFoundException.EXCEPTION);
        idempotency.fail(reason);
    }

    /**
     * 외부 결제 호출은 성공했으나 후처리가 실패한 경우. 돈은 이미 움직였으므로 FAILURE와 구분해 기록한다.
     */
    @Transactional
    public void markNeedsReconcile(Long idempotencyId, String reason, String paymentKey) {
        PaymentIdempotency idempotency = paymentIdempotencyRepository.findById(idempotencyId)
                .orElseThrow(() -> IdempotencyNotFoundException.EXCEPTION);
        idempotency.needsReconcile(reason, paymentKey);
    }
}
