package com.mavis.common.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ErrorResponse {
    private final boolean success = false;
    private final int status;
    private final String code;
    private final String reason;
    private final LocalDateTime timeStamp;

    private final String path;

    public ErrorResponse(ErrorReason errorReason, String path) {
        this.status = errorReason.status();
        this.code = errorReason.code();
        this.reason = errorReason.reason();
        this.timeStamp = LocalDateTime.now();
        this.path = path;
    }
}
