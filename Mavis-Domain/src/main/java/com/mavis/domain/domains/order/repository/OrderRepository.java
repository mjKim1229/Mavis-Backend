package com.mavis.domain.domains.order.repository;

import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.domain.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long>, OrderCustomRepository {
    Optional<Order> findByIdAndIsDeletedFalse(Long id);

    @Query("SELECT o FROM Order o JOIN FETCH o.orderItems WHERE o.id = :id AND o.isDeleted = false")
    Optional<Order> findByIdWithItemsAndIsDeletedFalse(@Param("id") Long id);
    List<Order> findByIdInAndIsDeletedFalse(List<Long> orderIds);
    Optional<Order> findByOrderIdAndIsDeletedFalse(String orderId);
    List<Order> findByOrderStatusAndCreatedAtBefore(OrderStatus orderStatus, LocalDateTime threshold);
    long countByOrderStatusAndIsDeletedFalse(OrderStatus orderStatus);
}
