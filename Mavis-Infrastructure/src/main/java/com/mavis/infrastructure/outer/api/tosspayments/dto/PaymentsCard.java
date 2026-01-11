package com.mavis.infrastructure.outer.api.tosspayments.dto;

public record PaymentsCard(
        Long amount,
        String issuerCode,
        CardCode acquirerCode,
        String number,
        Long installmentPlanMonths,
        String approveNo,
        Boolean useCardPoint,
        String cardType,
        String ownerType,
        CardAcquireStatus acquireStatus,
        Boolean isInterestFree,
        String interestPayer
) {}
