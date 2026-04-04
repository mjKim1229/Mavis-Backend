package com.mavis.infrastructure.outer.api.tosspayments.exception;

import com.mavis.common.exception.BaseErrorCode;
import com.mavis.common.exception.MavisCodeException;

public class TossPaymentsException extends MavisCodeException {

    public TossPaymentsException(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
