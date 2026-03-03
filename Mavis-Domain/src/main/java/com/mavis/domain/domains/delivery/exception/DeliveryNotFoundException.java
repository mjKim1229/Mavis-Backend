package com.mavis.domain.domains.delivery.exception;

import com.mavis.common.exception.MavisCodeException;

public class DeliveryNotFoundException extends MavisCodeException {

    public static final MavisCodeException EXCEPTION = new DeliveryNotFoundException();

    private DeliveryNotFoundException() {
        super(DeliveryErrorCode.DELIVERY_NOT_FOUND);
    }
}
