package com.mavis.api.auth.facade;

import com.mavis.api.auth.mapper.UserMapper;
import com.mavis.infrastructure.outer.api.oauth.client.KakaoInfoClient;
import com.mavis.infrastructure.outer.api.oauth.client.KakaoOAuthClient;
import com.mavis.infrastructure.outer.api.oauth.dto.KakaoOAuthRequest;
import com.mavis.infrastructure.outer.api.oauth.dto.KakaoTokenResponse;
import com.mavis.infrastructure.outer.api.oauth.dto.KakaoUserInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.mavis.common.consts.MavisStatic.BEARER;

@Component
@RequiredArgsConstructor
public class UserFacade {
    private final KakaoOAuthClient kakaoOAuthClient;
    private final KakaoInfoClient kakaoInfoClient;
    private final UserMapper userMapper;

    public KakaoUserInfoResponse register(String code) {
        KakaoOAuthRequest kakaoOAuthRequest = userMapper.fromCode(code);
        KakaoTokenResponse kakaoTokenResponse = kakaoOAuthClient.kakaoAuth(kakaoOAuthRequest);
        String bearerAccessToken = BEARER + kakaoTokenResponse.accessToken();
        return kakaoInfoClient.getUserInfo(bearerAccessToken);
    }
}
