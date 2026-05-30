package com.mavis.domain.domains.admin.exception;

import com.mavis.common.exception.MavisCodeException;

public class AdminInvalidPasswordException extends MavisCodeException {

    public static final MavisCodeException EXCEPTION = new AdminInvalidPasswordException();

    private AdminInvalidPasswordException() {
        super(AdminErrorCode.ADMIN_INVALID_PASSWORD);
    }
}
