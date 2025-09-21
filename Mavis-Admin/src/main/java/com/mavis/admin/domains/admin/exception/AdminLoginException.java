package com.mavis.admin.domains.admin.exception;

import com.mavis.common.exception.MavisCodeException;

public class AdminLoginException extends MavisCodeException {

    public static final MavisCodeException EXCEPTION = new AdminLoginException();
    public AdminLoginException() {
        super(AdminErrorCode.ADMIN_LOGIN);
    }
}
