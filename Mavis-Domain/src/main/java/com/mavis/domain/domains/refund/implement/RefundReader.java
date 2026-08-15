package com.mavis.domain.domains.refund.implement;

import com.mavis.domain.domains.order.domain.OrderItem;
import com.mavis.domain.domains.refund.domain.Refund;
import com.mavis.domain.domains.refund.exception.RefundNotFoundException;
import com.mavis.domain.domains.refund.repository.RefundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
        // 상태 무관 — 환불 이력(요청/완료/거절)이 있으면 재신청 차단
        return refundRepository.existsByOrderItem(orderItem);
    }
}
