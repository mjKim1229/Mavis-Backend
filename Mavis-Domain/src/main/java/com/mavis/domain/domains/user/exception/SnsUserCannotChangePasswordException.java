package com.mavis.domain.domains.user.exception;

import com.mavis.common.exception.MavisCodeException;

public class SnsUserCannotChangePasswordException extends MavisCodeException {

    public static final MavisCodeException EXCEPTION = new SnsUserCannotChangePasswordException();

    private SnsUserCannotChangePasswordException() {
        super(UserErrorCode.SNS_USER_CANNOT_CHANGE_PASSWORD);
    }
}
