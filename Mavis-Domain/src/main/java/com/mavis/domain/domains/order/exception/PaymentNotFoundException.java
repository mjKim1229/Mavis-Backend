package com.mavis.domain.domains.order.exception;

import com.mavis.common.exception.MavisCodeException;

public class PaymentNotFoundException extends MavisCodeException {
    public static final MavisCodeException EXCEPTION = new PaymentNotFoundException();

    private PaymentNotFoundException() {
        super(OrderErrorCode.PAYMENT_NOT_FOUND);
    }
}
