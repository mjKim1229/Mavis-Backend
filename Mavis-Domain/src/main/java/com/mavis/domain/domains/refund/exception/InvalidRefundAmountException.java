package com.mavis.domain.domains.refund.exception;

import com.mavis.common.exception.MavisCodeException;

public class InvalidRefundAmountException extends MavisCodeException {
    public static final MavisCodeException EXCEPTION = new InvalidRefundAmountException();

    private InvalidRefundAmountException() {
        super(RefundErrorCode.INVALID_REFUND_AMOUNT);
    }
}
