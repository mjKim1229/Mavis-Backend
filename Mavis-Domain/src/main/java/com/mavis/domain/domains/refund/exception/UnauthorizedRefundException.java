package com.mavis.domain.domains.refund.exception;

import com.mavis.common.exception.MavisCodeException;

public class UnauthorizedRefundException extends MavisCodeException {
    public static final MavisCodeException EXCEPTION = new UnauthorizedRefundException();

    private UnauthorizedRefundException() {
        super(RefundErrorCode.UNAUTHORIZED_REFUND);
    }
}
