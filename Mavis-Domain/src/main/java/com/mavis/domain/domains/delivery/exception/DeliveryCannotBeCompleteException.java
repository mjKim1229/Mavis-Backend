package com.mavis.domain.domains.delivery.exception;

import com.mavis.common.exception.MavisCodeException;

public class DeliveryCannotBeCompleteException extends MavisCodeException {
    public static final MavisCodeException EXCEPTION = new DeliveryCannotBeCompleteException();

    private DeliveryCannotBeCompleteException() {
        super(DeliveryErrorCode.DELIVERY_CANNOT_COMPLETE);
    }
}
