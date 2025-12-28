package com.mavis.domain.domains.order.repository;

import com.mavis.domain.domains.order.domain.PendingOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PendingOrderRepository extends JpaRepository<PendingOrder, Long> {
    Optional<PendingOrder> findByOrderIdAndAmountAndIsConfirmedFalse(String orderId, int amount);
}
