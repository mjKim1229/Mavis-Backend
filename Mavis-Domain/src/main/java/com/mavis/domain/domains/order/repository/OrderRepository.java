package com.mavis.domain.domains.order.repository;

import com.mavis.domain.domains.order.domain.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<OrderItem, Long> {
    Optional<OrderItem> findByIdAndIsDeletedFalse(Long id);
}
