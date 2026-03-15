package com.mavis.domain.domains.order.exception;

import com.mavis.common.exception.MavisCodeException;

public class DuplicatePaymentException extends MavisCodeException {
    public static final MavisCodeException EXCEPTION = new DuplicatePaymentException();

    private DuplicatePaymentException() {
        super(OrderErrorCode.DUPLICATE_PAYMENT);
    }
}
