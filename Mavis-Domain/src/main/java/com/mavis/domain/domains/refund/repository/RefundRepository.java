package com.mavis.domain.domains.refund.repository;

import com.mavis.domain.domains.order.domain.OrderItem;
import com.mavis.domain.domains.refund.domain.Refund;
import com.mavis.domain.domains.refund.domain.RefundStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RefundRepository extends JpaRepository<Refund, Long> {
    boolean existsByOrderItemAndRefundStatusIn(OrderItem orderItem, List<RefundStatus> statuses);
    Optional<Refund> findByOrderItem(OrderItem orderItem);
    Page<Refund> findByRefundStatus(RefundStatus refundStatus, Pageable pageable);
    long countByRefundStatus(RefundStatus refundStatus);
}
