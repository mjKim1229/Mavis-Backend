package com.mavis.domain.domains.refund.exception;

import com.mavis.common.exception.MavisCodeException;

public class RefundNotFoundException extends MavisCodeException {
    public static final MavisCodeException EXCEPTION = new RefundNotFoundException();

    private RefundNotFoundException() {
        super(RefundErrorCode.REFUND_NOT_FOUND);
    }
}
