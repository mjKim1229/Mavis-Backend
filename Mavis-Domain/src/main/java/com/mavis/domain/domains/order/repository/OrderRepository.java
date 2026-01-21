package com.mavis.domain.domains.order.repository;

import com.mavis.domain.domains.order.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long>, OrderCustomRepository {
    Optional<Order> findByIdAndIsDeletedFalse(Long id);
    List<Order> findByIdInAndIsDeletedFalse(List<Long> orderIds);
    Optional<Order> findByOrderIdAndIsDeletedFalse(String orderId);
}
