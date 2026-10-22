package com.mavis.api.refund.dto;

import com.mavis.domain.domains.claim.domain.ClaimReasonType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record RequestReturnRequest(
        @NotNull(message = "반품 사유 유형은 필수입니다.")
        ClaimReasonType reasonType,
        @NotEmpty(message = "반품할 상품은 필수입니다.")
        @Valid
        List<ReturnItemRequest> items
) {
    public List<Long> orderItemIds() {
        return items.stream()
                .map(ReturnItemRequest::orderItemId)
                .toList();
    }

    public Map<Long, String> reasonByOrderItemId() {
        Map<Long, String> reasons = new HashMap<>();
        items.forEach(item -> reasons.put(item.orderItemId(), item.reason()));
        return reasons;
    }

    public record ReturnItemRequest(
            @NotNull(message = "반품할 상품 ID는 필수입니다.")
            Long orderItemId,
            String reason
    ) {
    }
}
