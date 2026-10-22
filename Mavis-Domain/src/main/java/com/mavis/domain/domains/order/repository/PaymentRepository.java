package com.mavis.domain.domains.order.repository;

import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.domain.OrderStatus;
import com.mavis.domain.domains.order.domain.Payment;
import com.mavis.domain.domains.order.domain.PaymentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long>, PaymentCustomRepository {
    Optional<Payment> findByOrderAndPaymentType(Order order, PaymentType paymentType);
    Optional<Payment> findByPaymentKey(String paymentKey);

    @Query("SELECT COALESCE(SUM(p.cancelAmount), 0) FROM Payment p WHERE p.order = :order AND p.paymentType = :paymentType")
    int sumCancelAmountByOrderAndPaymentType(@Param("order") Order order, @Param("paymentType") PaymentType paymentType);
    List<Payment> findByOrderOrderStatusAndOrderCreatedAtBefore(OrderStatus status, LocalDateTime threshold);
}
