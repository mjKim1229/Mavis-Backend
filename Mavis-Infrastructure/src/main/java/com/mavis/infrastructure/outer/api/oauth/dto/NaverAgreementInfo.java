package com.mavis.infrastructure.outer.api.oauth.dto;

public record NaverAgreementInfo(
        String termCode,
        String clientId,
        String agreeDate
) {
}
