package com.mavis.common.dto;

import lombok.Builder;

@Builder
public record ErrorReason(
        Integer status,
        String code,
        String reason
) {
}
