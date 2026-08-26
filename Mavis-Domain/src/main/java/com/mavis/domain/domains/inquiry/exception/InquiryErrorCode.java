package com.mavis.domain.domains.inquiry.exception;

import com.mavis.common.dto.ErrorReason;
import com.mavis.common.exception.BaseErrorCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum InquiryErrorCode implements BaseErrorCode {
    INQUIRY_NOT_FOUND(404, "존재하지 않는 문의사항입니다.", "INQUIRY_404_1"),
    INQUIRY_ANSWER_NOT_FOUND(404, "존재하지 않는 문의 답변입니다.", "INQUIRY_404_2"),
    INQUIRY_ALREADY_ANSWERED(400, "이미 답변한 문의사항입니다.", "INQUIRY_400_1"),
    UNAUTHORIZED_INQUIRY(403, "문의 삭제 권한이 없습니다.", "INQUIRY_403_1"),
    INQUIRY_ALREADY_ANSWERED_CANNOT_DELETE(400, "답변이 완료된 문의는 삭제할 수 없습니다.", "INQUIRY_400_2");
    private final Integer status;
    private final String message;
    private final String code;

    @Override
    public ErrorReason getErrorReason() {
        return new ErrorReason(status, code, message);
    }
}
