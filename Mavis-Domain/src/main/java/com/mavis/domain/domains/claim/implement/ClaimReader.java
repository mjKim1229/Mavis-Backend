package com.mavis.domain.domains.claim.implement;

import com.mavis.domain.domains.claim.domain.Claim;
import com.mavis.domain.domains.claim.repository.ClaimItemRepository;
import com.mavis.domain.domains.claim.repository.ClaimRepository;
import com.mavis.domain.domains.order.domain.OrderItem;
import com.mavis.domain.domains.refund.exception.RefundNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClaimReader {

    private final ClaimRepository claimRepository;
    private final ClaimItemRepository claimItemRepository;

    public Claim findByIdWithOrder(Long claimId) {
        return claimRepository.findByIdWithOrder(claimId)
                .orElseThrow(() -> RefundNotFoundException.EXCEPTION);
    }

    public boolean hasClaim(OrderItem orderItem) {
        // 상태 무관 — 클레임 이력(요청/완료/거절)이 있으면 재신청 차단
        return claimItemRepository.existsByOrderItem(orderItem);
    }
}
