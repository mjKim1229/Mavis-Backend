package com.mavis.api.auth.dto;

public record KakaoOauthResponse(
        Long userId,
        String accessToken,
        String refreshToken
) {
}
