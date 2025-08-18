package com.mavis.api.auth.mapper;

import com.mavis.common.properties.NaverProperties;
import com.mavis.infrastructure.outer.api.oauth.dto.KakaoOAuthRequest;
import com.mavis.common.properties.KakaoProperties;
import com.mavis.infrastructure.outer.api.oauth.dto.NaverOAuthRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {
    private final KakaoProperties kakaoProperties;
    private final NaverProperties naverProperties;

    public KakaoOAuthRequest fromCode(String code) {
        return KakaoOAuthRequest.builder()
                .code(code)
                .redirectUrl(kakaoProperties.redirectUrl())
                .clientId(kakaoProperties.clientId())
                .build();
    }

    public NaverOAuthRequest fromNaverCode(String code) {
        return NaverOAuthRequest.builder()
                .code(code)
                .clientSecret(naverProperties.clientSecret())
                .clientId(naverProperties.clientId())
                .state(naverProperties.state())
                .build();
    }
}
