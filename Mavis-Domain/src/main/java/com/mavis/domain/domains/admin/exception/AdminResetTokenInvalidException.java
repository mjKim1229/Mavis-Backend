package com.mavis.domain.domains.admin.exception;

import com.mavis.common.exception.MavisCodeException;

public class AdminResetTokenInvalidException extends MavisCodeException {

    public static final MavisCodeException EXCEPTION = new AdminResetTokenInvalidException();

    private AdminResetTokenInvalidException() {
        super(AdminErrorCode.ADMIN_RESET_TOKEN_INVALID);
    }
}
