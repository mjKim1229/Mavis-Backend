package com.mavis.domain.domains.refund.exception;

import com.mavis.common.exception.MavisCodeException;

public class InvalidRefundQuantityException extends MavisCodeException {
    public static final MavisCodeException EXCEPTION = new InvalidRefundQuantityException();

    private InvalidRefundQuantityException() {
        super(RefundErrorCode.INVALID_REFUND_QUANTITY);
    }
}
