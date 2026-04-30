package com.mavis.infrastructure.outer.api.oauth.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;

@JsonNaming(SnakeCaseStrategy.class)
public record KakaoServiceTermsResponse(Long id, List<KakaoServiceTerm> serviceTerms) {

    @JsonNaming(SnakeCaseStrategy.class)
    public record KakaoServiceTerm(String tag, Boolean required, Boolean agreed, Boolean revocable) { }
}
