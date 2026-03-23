package com.mavis.domain.domains.refund.exception;

import com.mavis.common.exception.MavisCodeException;

public class CannotRefundException extends MavisCodeException {
    public static final MavisCodeException EXCEPTION = new CannotRefundException();

    private CannotRefundException() {
        super(RefundErrorCode.CANNOT_REFUND);
    }
}
