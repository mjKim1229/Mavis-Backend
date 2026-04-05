package com.mavis.domain.domains.order.implement;

import com.mavis.domain.domains.order.domain.PaymentApiType;
import com.mavis.domain.domains.order.domain.PaymentIdempotency;
import com.mavis.domain.domains.order.exception.DuplicatePaymentException;
import com.mavis.domain.domains.order.exception.IdempotencyNotFoundException;
import com.mavis.domain.domains.order.exception.PaymentAlreadyProcessingException;
import com.mavis.domain.domains.order.exception.PreviousPaymentFailedException;
import com.mavis.domain.domains.order.repository.PaymentIdempotencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PaymentIdempotencyManager {

    private static final int EXPIRY_DAYS = 15;

    private final PaymentIdempotencyRepository paymentIdempotencyRepository;

    public Optional<PaymentIdempotency> findExisting(String idempotencyKey, PaymentApiType apiType) {
        return paymentIdempotencyRepository.findByIdempotencyKeyAndApiType(idempotencyKey, apiType)
                .filter(h -> h.getExpiredAt().isAfter(LocalDateTime.now()));
    }

    public PaymentIdempotency startProcessing(String idempotencyKey, PaymentApiType apiType) {
        try {
            return paymentIdempotencyRepository.save(
                    PaymentIdempotency.ofProcessing(idempotencyKey, apiType, LocalDateTime.now().plusDays(EXPIRY_DAYS))
            );
        } catch (DataIntegrityViolationException e) {
            PaymentIdempotency existing = paymentIdempotencyRepository
                    .findByIdempotencyKeyAndApiType(idempotencyKey, apiType)
                    .orElseThrow(() -> IdempotencyNotFoundException.EXCEPTION);

            return switch (existing.getStatus()) {
                case PROCESSING -> throw PaymentAlreadyProcessingException.EXCEPTION;
                case SUCCESS -> throw DuplicatePaymentException.EXCEPTION;
                case FAILURE -> throw PreviousPaymentFailedException.EXCEPTION;
            };
        }
    }

    public void markSuccess(PaymentIdempotency idempotency) {
        idempotency.success();
        paymentIdempotencyRepository.save(idempotency);
    }

    public void markFailure(PaymentIdempotency idempotency, String reason) {
        idempotency.fail(reason);
        paymentIdempotencyRepository.save(idempotency);
    }
}
