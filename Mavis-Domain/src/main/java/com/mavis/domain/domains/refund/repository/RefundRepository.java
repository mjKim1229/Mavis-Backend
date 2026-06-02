package com.mavis.domain.domains.refund.repository;

import com.mavis.domain.domains.order.domain.OrderItem;
import com.mavis.domain.domains.refund.domain.Refund;
import com.mavis.domain.domains.refund.domain.RefundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RefundRepository extends JpaRepository<Refund, Long>, RefundCustomRepository {
    boolean existsByOrderItemAndRefundStatusIn(OrderItem orderItem, List<RefundStatus> statuses);
    Optional<Refund> findByOrderItem(OrderItem orderItem);
    long countByRefundStatus(RefundStatus refundStatus);

    @Query("SELECT r FROM Refund r JOIN FETCH r.orderItem oi JOIN FETCH oi.order WHERE r.id = :id")
    Optional<Refund> findByIdWithOrderItemAndOrder(@Param("id") Long id);
}
