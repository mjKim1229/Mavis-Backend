package com.mavis.domain.domains.order.repository;

import com.mavis.domain.domains.order.domain.PaymentApiType;
import com.mavis.domain.domains.order.domain.PaymentIdempotency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentIdempotencyRepository extends JpaRepository<PaymentIdempotency, Long> {
    Optional<PaymentIdempotency> findByIdempotencyKeyAndApiType(String idempotencyKey, PaymentApiType apiType);
}
