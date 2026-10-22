package com.mavis.domain.domains.refund.exception;

import com.mavis.common.dto.ErrorReason;
import com.mavis.common.exception.BaseErrorCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum RefundErrorCode implements BaseErrorCode {
    REFUND_NOT_FOUND(404, "환불 정보를 찾을 수 없습니다.", "REFUND_404_1"),
    CANNOT_REFUND(400, "환불이 불가능한 상태입니다.", "REFUND_400_1"),
    INVALID_REFUND_AMOUNT(400, "환불 금액은 1원 이상, 환불 가능 잔액 이하여야 합니다.", "REFUND_400_2"),
    INVALID_REFUND_DECISION(400, "반품 사유에 맞지 않는 환불 처리 방식입니다.", "REFUND_400_3"),
    ALREADY_REFUND_REQUESTED(409, "이미 환불 처리 이력이 있는 주문 상품입니다.", "REFUND_409_1"),
    UNAUTHORIZED_REFUND(403, "본인의 주문에 대해서만 반품 신청이 가능합니다.", "REFUND_403_1");

    private final Integer status;
    private final String message;
    private final String code;

    @Override
    public ErrorReason getErrorReason() {
        return new ErrorReason(status, code, message);
    }
}
