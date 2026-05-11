package com.mavis.domain.domains.order.implement;

import com.mavis.domain.domains.order.domain.PaymentApiType;
import com.mavis.domain.domains.order.domain.PaymentIdempotency;
import com.mavis.domain.domains.order.repository.PaymentIdempotencyRepository;
import com.mavis.domain.support.RepositoryTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentIdempotencyRepositoryConstraintTest extends RepositoryTestSupport {

    @Autowired
    private PaymentIdempotencyRepository repository;

    @Test
    void 동일한_idempotencyKey와_apiType으로_중복_저장시_DataIntegrityViolationException_던진다() {
        repository.saveAndFlush(
            PaymentIdempotency.ofProcessing("key-001", PaymentApiType.CONFIRM, LocalDateTime.now().plusDays(15))
        );

        assertThatThrownBy(() ->
            repository.saveAndFlush(
                PaymentIdempotency.ofProcessing("key-001", PaymentApiType.CONFIRM, LocalDateTime.now().plusDays(15))
            )
        ).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void 동일한_key라도_apiType이_다르면_정상_저장된다() {
        repository.saveAndFlush(
            PaymentIdempotency.ofProcessing("key-001", PaymentApiType.CONFIRM, LocalDateTime.now().plusDays(15))
        );
        repository.saveAndFlush(
            PaymentIdempotency.ofProcessing("key-001", PaymentApiType.CANCEL, LocalDateTime.now().plusDays(15))
        );
    }
}
