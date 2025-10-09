package com.mavis.infrastructure.outer.api.tosspayments.dto;

import com.mavis.common.enums.EnumMapperType;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PaymentsStatus implements EnumMapperType {
    READY("READY"),
    IN_PROGRESS("IN_PROGRESS"),
    WAITING_FOR_DEPOSIT("WAITING_FOR_DEPOSIT"),
    DONE("DONE"),
    CANCELED("CANCELED"),
    PARTIAL_CANCELED("PARTIAL_CANCELED"),
    ABORTED("ABORTED"),
    EXPIRED("EXPIRED");

    private final String title;

    @Override
    public String getCode() {
        return name();
    }

    @Override
    public String getTitle() {
        return title;
    }
}
