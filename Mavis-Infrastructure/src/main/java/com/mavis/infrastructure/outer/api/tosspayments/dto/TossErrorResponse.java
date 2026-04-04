package com.mavis.infrastructure.outer.api.tosspayments.dto;

public record TossErrorResponse(
        String version,
        String traceId,
        Error error
) {
    public record Error(String code, String message) {}
}
