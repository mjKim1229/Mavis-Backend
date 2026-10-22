package com.mavis.domain.domains.claim.implement;

import com.mavis.domain.domains.claim.domain.Claim;
import com.mavis.domain.domains.claim.domain.ClaimStatus;
import com.mavis.domain.domains.claim.domain.ClaimType;
import com.mavis.domain.domains.claim.repository.ClaimItemRepository;
import com.mavis.domain.domains.claim.repository.ClaimRepository;
import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.domain.OrderItem;
import com.mavis.domain.domains.refund.exception.RefundNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ClaimReader {

    private final ClaimRepository claimRepository;
    private final ClaimItemRepository claimItemRepository;

    public Claim findByIdWithOrder(Long claimId) {
        return claimRepository.findByIdWithOrder(claimId)
                .orElseThrow(() -> RefundNotFoundException.EXCEPTION);
    }

    public boolean hasAnyClaim(List<OrderItem> orderItems) {
        // 상태 무관 — 클레임 이력(요청/완료/거절)이 있으면 재신청 차단
        return claimItemRepository.existsByOrderItemIn(orderItems);
    }

    public boolean hasCompletedReturn(Order order) {
        return claimRepository.existsByOrderAndClaimTypeAndClaimStatus(order, ClaimType.RETURN, ClaimStatus.COMPLETED);
    }

    public Set<Long> findOrderIdsWithCompletedReturn(List<Order> orders) {
        List<Long> orderIds = claimRepository.findOrderIdsByOrderInAndClaimTypeAndClaimStatus(
                orders, ClaimType.RETURN, ClaimStatus.COMPLETED);
        return new HashSet<>(orderIds);
    }
}
