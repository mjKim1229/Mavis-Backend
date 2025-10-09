package com.mavis.infrastructure.outer.api.tosspayments.dto;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CardAcquireStatus {
    READY("READY"),
    REQUESTED("REQUESTED"),
    COMPLETED("COMPLETED"),
    CANCEL_REQUESTED("CANCEL_REQUESTED"),
    CANCELED("CANCELED");

    private final String value;
}
