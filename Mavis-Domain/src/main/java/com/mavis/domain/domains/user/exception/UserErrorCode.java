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
    EMAIL_NOT_VERIFIED(400, "USER_400_5", "이메일 인증이 필요합니다."),
    SNS_USER_CANNOT_CHANGE_PASSWORD(400, "USER_400_4", "SNS 로그인 사용자는 비밀번호를 변경할 수 없습니다."),
    DUPLICATE_EMAIL(409, "USER_409_1", "이미 가입된 이메일입니다."),
    DUPLICATE_USERNAME(409, "USER_409_2", "이미 사용 중인 아이디입니다."),
    VERIFICATION_ATTEMPTS_EXCEEDED(429, "USER_429_1", "인증번호 입력 횟수를 초과했습니다. 인증번호를 다시 요청해주세요."),
    VERIFICATION_RESEND_TOO_SOON(429, "USER_429_2", "인증번호는 1분 후에 다시 요청할 수 있습니다."),
    VERIFICATION_SEND_LIMIT_EXCEEDED(429, "USER_429_3", "인증번호 발송 한도를 초과했습니다. 24시간 후 다시 시도해주세요.");

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
