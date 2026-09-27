package com.mavis.domain.domains.user.exception;

import com.mavis.common.exception.MavisCodeException;

public class VerificationAttemptsExceededException extends MavisCodeException {

    public static final MavisCodeException EXCEPTION = new VerificationAttemptsExceededException();

    private VerificationAttemptsExceededException() {
        super(UserErrorCode.VERIFICATION_ATTEMPTS_EXCEEDED);
    }
}
