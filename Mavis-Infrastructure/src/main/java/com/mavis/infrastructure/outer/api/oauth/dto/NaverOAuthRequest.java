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
}
