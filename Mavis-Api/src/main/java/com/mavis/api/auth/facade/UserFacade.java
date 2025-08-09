package com.mavis.api.auth.facade;

import com.mavis.api.auth.dto.UserKakaoOauthResponse;
import com.mavis.api.auth.implement.UserJwtGenerator;
import com.mavis.api.auth.mapper.UserMapper;
import com.mavis.common.dto.JwtPair;
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
    private final UserJwtGenerator userJwtGenerator;

    public UserKakaoOauthResponse register(String code) {
        KakaoOAuthRequest kakaoOAuthRequest = userMapper.fromCode(code);
        KakaoTokenResponse kakaoTokenResponse = kakaoOAuthClient.kakaoAuth(kakaoOAuthRequest);

        String bearerAccessToken = BEARER + kakaoTokenResponse.accessToken();
        KakaoUserInfoResponse userInfo = kakaoInfoClient.getUserInfo(bearerAccessToken);

        //TODO user 저장 service
        JwtPair jwtPair = userJwtGenerator.getJwtPair(userInfo.id());
        return new UserKakaoOauthResponse(userInfo.id(), jwtPair);
    }
}
