package com.mavis.api.auth.facade;

import com.mavis.api.auth.dto.UserOauthResponse;
import com.mavis.api.auth.implement.UserJwtGenerator;
import com.mavis.api.auth.mapper.UserMapper;
import com.mavis.api.auth.service.UserService;
import com.mavis.common.dto.JwtPair;
import com.mavis.common.properties.KakaoProperties;
import com.mavis.common.properties.NaverProperties;
import com.mavis.infrastructure.outer.api.oauth.client.kakao.KakaoInfoClient;
import com.mavis.infrastructure.outer.api.oauth.client.kakao.KakaoOAuthClient;
import com.mavis.infrastructure.outer.api.oauth.client.naver.NaverInfoClient;
import com.mavis.infrastructure.outer.api.oauth.client.naver.NaverOAuthClient;
import com.mavis.infrastructure.outer.api.oauth.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.mavis.common.consts.MavisStatic.BEARER;

@Component
@RequiredArgsConstructor
public class UserFacade {
    private final KakaoOAuthClient kakaoOAuthClient;
    private final KakaoInfoClient kakaoInfoClient;
    private final NaverOAuthClient naverOAuthClient;
    private final UserMapper userMapper;
    private final UserJwtGenerator userJwtGenerator;
    private final KakaoProperties kakaoProperties;
    private final NaverInfoClient naverInfoClient;
    private final NaverProperties naverProperties;
    private final UserService userService;

    public UserOauthResponse register(String code, String url) {
        KakaoOAuthRequest kakaoOAuthRequest = userMapper.fromCode(code, url);
        KakaoTokenResponse kakaoTokenResponse = kakaoOAuthClient.kakaoAuth(kakaoOAuthRequest);

        String bearerAccessToken = BEARER + kakaoTokenResponse.accessToken();
        KakaoUserInfoResponse userInfo = kakaoInfoClient.getUserInfo(bearerAccessToken);

        //TODO user 저장 service
        JwtPair jwtPair = userJwtGenerator.getJwtPair(userInfo.id());
        return new UserOauthResponse(userInfo.id(), jwtPair);
    }

    public void withDrawKakao() {
    }

    private void unlinkKakao(Long snsId) {
        String header = "KakaoAK " + kakaoProperties.adminKey();
        UnlinkKaKaoTarget unlinkKakaoTarget = UnlinkKaKaoTarget.from(snsId);
        kakaoInfoClient.unlink(header, unlinkKakaoTarget);
    }

    public UserOauthResponse registerNaver(String code) {
        NaverOAuthRequest naverOAuthRequest = userMapper.fromNaverCode(code);
        NaverTokenResponse naverTokenResponse = naverOAuthClient.naverAuth(naverOAuthRequest);
        String bearerAccessToken = BEARER + naverTokenResponse.accessToken();
        NaverUserInfoResponse userInfo = naverInfoClient.getUserInfo(bearerAccessToken);
        Long userId = userService.upsertNaverUser(userInfo.response());
        JwtPair jwtPair = userJwtGenerator.getJwtPair(userId);
        return new UserOauthResponse(userId, jwtPair);
    }

    public void withDrawNaver() {
        NaverTokenRevokeRequest.builder()
                .clientId(naverProperties.clientId())
                .grantType("delete");
    }
}
