package com.mavis.common.exception;

import com.mavis.common.dto.ErrorReason;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static com.mavis.common.consts.MavisStatic.*;

@Getter
@RequiredArgsConstructor
public enum GlobalErrorCode implements BaseErrorCode {
    TOKEN_EXPIRED(UNAUTHORIZED, "AUTH_401_1", "인증 시간이 만료되었습니다. 인증토큰을 재 발급 해주세요"),
    REFRESH_TOKEN_EXPIRED(FORBIDDEN, "AUTH_403_1", "인증 시간이 만료되었습니다. 재 로그인 해주세요."),
    INVALID_TOKEN(UNAUTHORIZED, "GLOBAL_401_1", "잘못된 토큰입니다. 재 로그인 해주세요"),
    INTERNAL_SERVER_ERROR(INTERNAL_SERVER, "GLOBAL_500_1", "서버 오류. 관리자에게 문의 바랍니다.");

    private final Integer status;
    private final String code;
    private final String reason;

    @Override
    public ErrorReason getErrorReason() {
        return new ErrorReason(status, code, reason);
    }
}
