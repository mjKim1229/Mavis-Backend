package com.mavis.common.dto;

import java.time.LocalDateTime;

public record SuccessResponse(
        boolean success,
        LocalDateTime timeStamp,
        Object data
) {
    public SuccessResponse(Object data) {
        this(true, LocalDateTime.now(), data);
    }
}
