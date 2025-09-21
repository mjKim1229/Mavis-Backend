package com.mavis.domains.admin.exception;

import com.mavis.common.dto.ErrorReason;
import com.mavis.common.exception.BaseErrorCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum AdminErrorCode implements BaseErrorCode {
    ADMIN_LOGIN(401, "아이디, 비밀번호를 확인해주세요", "ADMIN_401_1");

    private final Integer status;
    private final String message;
    private final String code;

    @Override
    public ErrorReason getErrorReason() {
        return new ErrorReason(status, message, code);
    }
}
