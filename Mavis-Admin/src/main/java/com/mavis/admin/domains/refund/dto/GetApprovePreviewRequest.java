package com.mavis.admin.domains.refund.dto;

import com.mavis.domain.domains.claim.domain.ReturnDecision;

public record GetApprovePreviewRequest(
        ReturnDecision decision,
        Integer amount
) {
}
