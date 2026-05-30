package com.mavis.domain.domains.admin.exception;

import com.mavis.common.exception.MavisCodeException;

public class AdminResetTokenExpiredException extends MavisCodeException {

    public static final MavisCodeException EXCEPTION = new AdminResetTokenExpiredException();

    private AdminResetTokenExpiredException() {
        super(AdminErrorCode.ADMIN_RESET_TOKEN_EXPIRED);
    }
}
