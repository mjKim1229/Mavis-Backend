package com.mavis.common.exception;

import com.mavis.common.dto.ErrorReason;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static com.mavis.common.consts.MavisStatic.INTERNAL_SERVER;

@Getter
@RequiredArgsConstructor
public enum GlobalErrorCode implements BaseErrorCode {
    INTERNAL_SERVER_ERROR(INTERNAL_SERVER, "GLOBAL_500_1", "서버 오류. 관리자에게 문의 바랍니다.");

    private final Integer status;
    private final String code;
    private final String reason;

    @Override
    public ErrorReason getErrorReason() {
        return new ErrorReason(status, code, reason);
    }
}
