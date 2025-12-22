package com.mavis.infrastructure.outer.api.oauth.dto;

import feign.form.FormProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class NaverOAuthRequest {
    @FormProperty("client_id")
    private String clientId;
    @FormProperty("client_secret")
    private String clientSecret;
    @FormProperty("code")
    private String code;
    @FormProperty("state")
    private String state;
    @FormProperty("access_token")
    private String accessToken;
    @FormProperty("refresh_token")
    private String refreshToken;
    @FormProperty("grant_type")
    private String grantType;
    @FormProperty("service_provider")
    private String serviceProvider;
}
