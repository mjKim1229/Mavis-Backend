package com.mavis.domain.domains.order.repository;

import com.mavis.domain.domains.order.domain.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long>, OrderCustomRepository {
    Optional<OrderItem> findByIdAndIsDeletedFalse(Long id);
}
