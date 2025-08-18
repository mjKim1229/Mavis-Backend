package com.mavis.infrastructure.outer.api.oauth.dto;

import feign.form.FormProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class NaverTokenRevokeRequest {
    @FormProperty("client_id")
    private String clientId;
    @FormProperty("client_secret")
    private String clientSecret;
    @FormProperty("access_token")
    private String accessToken;
    @FormProperty("grant_type")
    private String grantType;
}
