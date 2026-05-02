package com.mavis.api.auth.mapper;

import com.mavis.common.properties.KakaoProperties;
import com.mavis.common.properties.NaverProperties;
import com.mavis.infrastructure.outer.api.oauth.dto.KakaoOAuthRequest;
import com.mavis.infrastructure.outer.api.oauth.dto.NaverOAuthRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuthMapper {
    private final KakaoProperties kakaoProperties;
    private final NaverProperties naverProperties;

    public KakaoOAuthRequest kakaoAuthRequest(String code, String url) {
        return KakaoOAuthRequest.builder()
                .code(code)
                .redirectUrl(url + kakaoProperties.redirectUrl())
                .clientId(kakaoProperties.clientId())
                .build();
    }

    public String kakaoAdminHeader() {
        return "KakaoAK " + kakaoProperties.adminKey();
    }

    public NaverOAuthRequest naverAuthRequest(String code) {
        return NaverOAuthRequest.builder()
                .code(code)
                .clientSecret(naverProperties.clientSecret())
                .clientId(naverProperties.clientId())
                .state(naverProperties.state())
                .grantType("authorization_code")
                .build();
    }

    public NaverOAuthRequest naverRefreshTokenRequest(String refreshToken) {
        return NaverOAuthRequest.builder()
                .clientId(naverProperties.clientId())
                .clientSecret(naverProperties.clientSecret())
                .refreshToken(refreshToken)
                .grantType("refresh_token")
                .build();
    }

    public NaverOAuthRequest naverDeleteRequest(String accessToken) {
        return NaverOAuthRequest.builder()
                .clientId(naverProperties.clientId())
                .clientSecret(naverProperties.clientSecret())
                .accessToken(accessToken)
                .serviceProvider("NAVER")
                .grantType("delete")
                .build();
    }
}
