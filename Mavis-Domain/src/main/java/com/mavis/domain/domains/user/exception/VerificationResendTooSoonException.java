package com.mavis.domain.domains.user.exception;

import com.mavis.common.exception.MavisCodeException;

public class VerificationResendTooSoonException extends MavisCodeException {

    public static final MavisCodeException EXCEPTION = new VerificationResendTooSoonException();

    private VerificationResendTooSoonException() {
        super(UserErrorCode.VERIFICATION_RESEND_TOO_SOON);
    }
}
