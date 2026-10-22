package com.mavis.domain.domains.refund.exception;

import com.mavis.common.exception.MavisCodeException;

public class InvalidRefundDecisionException extends MavisCodeException {
    public static final MavisCodeException EXCEPTION = new InvalidRefundDecisionException();

    private InvalidRefundDecisionException() {
        super(RefundErrorCode.INVALID_REFUND_DECISION);
    }
}
