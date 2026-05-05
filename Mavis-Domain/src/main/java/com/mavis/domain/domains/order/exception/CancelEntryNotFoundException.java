package com.mavis.domain.domains.order.exception;

import com.mavis.common.exception.MavisCodeException;

public class CancelEntryNotFoundException extends MavisCodeException {
    public static final MavisCodeException EXCEPTION = new CancelEntryNotFoundException();

    private CancelEntryNotFoundException() {
        super(OrderErrorCode.CANCEL_ENTRY_NOT_FOUND);
    }
}
