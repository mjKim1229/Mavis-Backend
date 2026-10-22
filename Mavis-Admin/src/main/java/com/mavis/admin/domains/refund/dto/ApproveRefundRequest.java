package com.mavis.admin.domains.refund.dto;

import com.mavis.domain.domains.claim.domain.ReturnDecision;

public record ApproveRefundRequest(
        ReturnDecision decision,
        Integer amount
) {
}
