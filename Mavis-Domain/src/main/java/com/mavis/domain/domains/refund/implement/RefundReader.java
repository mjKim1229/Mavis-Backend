package com.mavis.domain.domains.refund.implement;

import com.mavis.domain.domains.order.domain.OrderItem;
import com.mavis.domain.domains.refund.domain.Refund;
import com.mavis.domain.domains.refund.domain.RefundStatus;
import com.mavis.domain.domains.refund.exception.RefundNotFoundException;
import com.mavis.domain.domains.refund.repository.RefundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RefundReader {

    private final RefundRepository refundRepository;

    public Refund findById(Long refundId) {
        return refundRepository.findById(refundId)
                .orElseThrow(() -> RefundNotFoundException.EXCEPTION);
    }

    public Refund findByIdWithOrderItemAndOrder(Long refundId) {
        return refundRepository.findByIdWithOrderItemAndOrder(refundId)
                .orElseThrow(() -> RefundNotFoundException.EXCEPTION);
    }

    public boolean hasActiveRefund(OrderItem orderItem) {
        return refundRepository.existsByOrderItemAndRefundStatusIn(
                orderItem,
                List.of(RefundStatus.REQUESTED)
        );
    }

    public Optional<RefundStatus> findRefundStatusByOrderItem(OrderItem orderItem) {
        return refundRepository.findByOrderItem(orderItem).map(Refund::getRefundStatus);
    }
}
