package com.mavis.api.auth.mapper;

import com.mavis.infrastructure.outer.api.oauth.dto.KakaoOAuthRequest;
import com.mavis.common.properties.KakaoProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {
    private final KakaoProperties kakaoProperties;

    public KakaoOAuthRequest fromCode(String code) {
        return KakaoOAuthRequest.builder()
                .code(code)
                .redirectUrl(kakaoProperties.redirectUrl())
                .clientId(kakaoProperties.clientId())
                .build();
    }
}
