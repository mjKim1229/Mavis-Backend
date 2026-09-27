package com.mavis.domain.domains.user.exception;

import com.mavis.common.exception.MavisCodeException;

public class VerificationSendLimitExceededException extends MavisCodeException {

    public static final MavisCodeException EXCEPTION = new VerificationSendLimitExceededException();

    private VerificationSendLimitExceededException() {
        super(UserErrorCode.VERIFICATION_SEND_LIMIT_EXCEEDED);
    }
}
