package com.mavis.domain.domains.admin.exception;

import com.mavis.common.dto.ErrorReason;
import com.mavis.common.exception.BaseErrorCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum AdminErrorCode implements BaseErrorCode {
    ADMIN_LOGIN(401, "아이디, 비밀번호를 확인해주세요", "ADMIN_401_1"),
    ADMIN_INVALID_PASSWORD(401, "현재 비밀번호가 올바르지 않습니다", "ADMIN_401_2"),
    ADMIN_NOT_FOUND(404, "존재하지 않는 관리자입니다", "ADMIN_404_1"),
    ADMIN_RESET_TOKEN_INVALID(400, "유효하지 않은 비밀번호 재설정 토큰입니다", "ADMIN_400_1"),
    ADMIN_RESET_TOKEN_EXPIRED(400, "만료된 비밀번호 재설정 토큰입니다", "ADMIN_400_2");

    private final Integer status;
    private final String message;
    private final String code;

    @Override
    public ErrorReason getErrorReason() {
        return new ErrorReason(status, message, code);
    }
}
