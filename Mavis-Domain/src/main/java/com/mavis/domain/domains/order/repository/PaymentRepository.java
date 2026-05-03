package com.mavis.domain.domains.order.repository;

import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.domain.OrderStatus;
import com.mavis.domain.domains.order.domain.Payment;
import com.mavis.domain.domains.order.domain.PaymentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderAndPaymentType(Order order, PaymentType paymentType);
    boolean existsByPaymentKey(String paymentKey);
    Payment findByOrderOrderId(String orderId);
    Optional<Payment> findByPaymentKey(String paymentKey);
    List<Payment> findByOrderOrderStatusAndOrderCreatedAtBefore(OrderStatus status, LocalDateTime threshold);
    List<Payment> findAllByOrder(Order order);
}
