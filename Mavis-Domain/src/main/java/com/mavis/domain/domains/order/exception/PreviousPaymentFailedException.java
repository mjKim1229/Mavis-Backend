package com.mavis.domain.domains.order.exception;

import com.mavis.common.exception.MavisCodeException;

public class PreviousPaymentFailedException extends MavisCodeException {
    public static final MavisCodeException EXCEPTION = new PreviousPaymentFailedException();

    private PreviousPaymentFailedException() {
        super(OrderErrorCode.PREVIOUS_PAYMENT_FAILED);
    }
}
