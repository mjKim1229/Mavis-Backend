package com.mavis.domain.domains.order.exception;

import com.mavis.common.exception.MavisCodeException;

public class IdempotencyNotFoundException extends MavisCodeException {
    public static final MavisCodeException EXCEPTION = new IdempotencyNotFoundException();

    private IdempotencyNotFoundException() {
        super(OrderErrorCode.IDEMPOTENCY_NOT_FOUND);
    }
}
