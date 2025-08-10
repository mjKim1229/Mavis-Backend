package com.mavis.common.exception;

import com.mavis.common.dto.ErrorReason;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class MavisCodeException extends RuntimeException {
    private BaseErrorCode errorCode;

    public ErrorReason getErrorReason() {
        return this.errorCode.getErrorReason();
    }
}
