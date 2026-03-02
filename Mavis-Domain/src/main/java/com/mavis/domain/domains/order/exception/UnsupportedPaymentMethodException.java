package com.mavis.domain.domains.order.exception;

import com.mavis.common.exception.MavisCodeException;

public class UnsupportedPaymentMethodException extends MavisCodeException {
    public static final MavisCodeException EXCEPTION = new UnsupportedPaymentMethodException();

    private UnsupportedPaymentMethodException() {
        super(OrderErrorCode.UNSUPPORTED_PAYMENT_METHOD);
    }
}
