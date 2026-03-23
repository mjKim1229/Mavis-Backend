package com.mavis.domain.domains.refund.exception;

import com.mavis.common.exception.MavisCodeException;

public class AlreadyRefundRequestedException extends MavisCodeException {
    public static final MavisCodeException EXCEPTION = new AlreadyRefundRequestedException();

    private AlreadyRefundRequestedException() {
        super(RefundErrorCode.ALREADY_REFUND_REQUESTED);
    }
}
