package com.mavis.domain.domains.notice.exception;

import com.mavis.common.dto.ErrorReason;
import com.mavis.common.exception.BaseErrorCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum NoticeErrorCode implements BaseErrorCode {
    NOTICE_NOT_FOUND(404, "공지사항을 찾을 수 없습니다", "NOTICE_404_1");

    private final Integer status;
    private final String message;
    private final String code;

    @Override
    public ErrorReason getErrorReason() {
        return new ErrorReason(status, code, message);
    }
}
