package com.mavis.infrastructure.outer.api.oauth.dto;

import java.util.List;

public record NaverAgreementResponse(
        String result,
        String accessToken,
        List<NaverAgreementInfo> agreementInfos
) {
}
