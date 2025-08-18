package com.mavis.infrastructure.outer.api.oauth.dto;

import feign.form.FormProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/*
회원탈퇴시 저장된 refresh Token으로 재발급 API 호출후 탈퇴 API 호출
 */
@Getter
@Builder
@AllArgsConstructor
public class NaverTokenRefreshRequest {
    @FormProperty("client_id")
    private String clientId;
    @FormProperty("client_secret")
    private String clientSecret;
    @FormProperty("refresh_Token")
    private String refreshToken;
    @FormProperty("grant_type")
    private String grantType;
}
