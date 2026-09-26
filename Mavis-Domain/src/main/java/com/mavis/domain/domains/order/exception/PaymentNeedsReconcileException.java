package com.mavis.domain.domains.order.exception;

import com.mavis.common.exception.MavisCodeException;

import static com.mavis.domain.domains.order.exception.OrderErrorCode.PAYMENT_NEEDS_RECONCILE;

public class PaymentNeedsReconcileException extends MavisCodeException {

    public static final MavisCodeException EXCEPTION = new PaymentNeedsReconcileException();

    private PaymentNeedsReconcileException() {
        super(PAYMENT_NEEDS_RECONCILE);
    }
}
