package com.mavis.domain.domains.user.exception;

import com.mavis.common.dto.ErrorReason;
import com.mavis.common.exception.BaseErrorCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum UserErrorCode implements BaseErrorCode {

    USER_NOT_FOUND(404, "USER_404_1", "존재하지 않는 회원입니다."),
    INVALID_VERIFICATION_CODE(400, "USER_400_1", "올바르지 않은 인증번호입니다"),
    VERIFICATION_CODE_EXPIRED(400, "USER_400_2", "인증번호가 만료되었습니다.");

    private final Integer status;
    private final String code;
    private final String reason;

    @Override
    public ErrorReason getErrorReason() {
        return ErrorReason.builder()
                .status(status)
                .code(code)
                .reason(reason)
                .build();
    }
}
