package com.mavis.domain.domains.order.repository;

import com.mavis.domain.domains.order.domain.Payment;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentCustomRepository {
    List<Payment> findExpiredVirtualAccountPayments(LocalDateTime now);
}
