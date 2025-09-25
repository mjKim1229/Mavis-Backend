package com.mavis.domain.domains.admin.exception;

import com.mavis.common.exception.BaseErrorCode;
import com.mavis.common.exception.MavisCodeException;

public class AdminNotFoundException extends MavisCodeException {

    public static final AdminNotFoundException EXCEPTION = new AdminNotFoundException();
    public AdminNotFoundException() {
        super(AdminErrorCode.ADMIN_NOT_FOUND);
    }
}
