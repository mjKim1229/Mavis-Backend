package com.mavis.domain.domains.delivery.exception;

import com.mavis.common.dto.ErrorReason;
import com.mavis.common.exception.BaseErrorCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum DeliveryErrorCode implements BaseErrorCode {
    DELIVERY_NOT_FOUND(404, "배송 정보를 찾을 수 없습니다", "DELIVERY_404_1"),
    DELIVERY_CANNOT_COMPLETE(400, "현재 상태에서는 배송완료로 변경할 수 없습니다.", "DELIVERY_400_1");

    private final Integer status;
    private final String message;
    private final String code;

    @Override
    public ErrorReason getErrorReason() {
        return new ErrorReason(status, code, message);
    }
}
