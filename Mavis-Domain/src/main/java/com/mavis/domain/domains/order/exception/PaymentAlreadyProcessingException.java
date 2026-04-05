package com.mavis.domain.domains.order.exception;

import com.mavis.common.exception.MavisCodeException;

public class PaymentAlreadyProcessingException extends MavisCodeException {
    public static final MavisCodeException EXCEPTION = new PaymentAlreadyProcessingException();

    private PaymentAlreadyProcessingException() {
        super(OrderErrorCode.PAYMENT_ALREADY_PROCESSING);
    }
}
