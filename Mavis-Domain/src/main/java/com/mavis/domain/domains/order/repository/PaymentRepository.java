package com.mavis.domain.domains.order.repository;

import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrder(Order order);
    boolean existsByPaymentKey(String paymentKey);
    Payment findByOrderOrderId(String orderId);
}
