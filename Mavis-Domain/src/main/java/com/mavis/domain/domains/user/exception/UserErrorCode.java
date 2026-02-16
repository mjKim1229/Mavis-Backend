package com.mavis.domain.domains.user.exception;

import com.mavis.common.dto.ErrorReason;
import com.mavis.common.exception.BaseErrorCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum UserErrorCode implements BaseErrorCode {

    USER_NOT_FOUND(404, "USER_404_1", "존재하지 않는 회원입니다."),
    INVALID_VERIFICATION_CODE(400, "USER_400_1", "올바르지 않은 인증번호입니다"),
    VERIFICATION_CODE_EXPIRED(400, "USER_400_2", "인증번호가 만료되었습니다."),
    INVALID_PASSWORD(400, "USER_400_3", "비밀번호가 올바르지 않습니다."),
    SNS_USER_CANNOT_CHANGE_PASSWORD(400, "USER_400_4", "SNS 로그인 사용자는 비밀번호를 변경할 수 없습니다.");

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
