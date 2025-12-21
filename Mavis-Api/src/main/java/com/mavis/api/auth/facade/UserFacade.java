package com.mavis.api.auth.facade;

import com.mavis.api.auth.dto.UserOauthResponse;
import com.mavis.api.auth.implement.UserJwtGenerator;
import com.mavis.api.auth.implement.UserReader;
import com.mavis.api.auth.mapper.UserMapper;
import com.mavis.api.auth.service.UserService;
import com.mavis.common.dto.JwtPair;
import com.mavis.common.properties.KakaoProperties;
import com.mavis.common.properties.NaverProperties;
import com.mavis.domain.domains.user.domain.SnsType;
import com.mavis.domain.domains.user.domain.User;
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
    private final UserReader userReader;

    public UserOauthResponse register(String code, String url) {
        KakaoOAuthRequest kakaoOAuthRequest = userMapper.fromCode(code, url);
        KakaoTokenResponse kakaoTokenResponse = kakaoOAuthClient.kakaoAuth(kakaoOAuthRequest);

        String bearerAccessToken = BEARER + kakaoTokenResponse.accessToken();
        KakaoUserInfoResponse userInfo = kakaoInfoClient.getUserInfo(bearerAccessToken);
        Long userId = userService.upsertKakaouser(userInfo);
        JwtPair jwtPair = userJwtGenerator.getJwtPair(userId);
        return new UserOauthResponse(userId, jwtPair);
    }

    public void withDrawKakao() {
        User user = userReader.getCurrentUser();
        Long snsId = Long.valueOf(user.getSnsId());
        unlinkKakao(snsId);
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
        Long userId = userService.upsertNaverUser(userInfo.response(), naverTokenResponse.refreshToken());
        JwtPair jwtPair = userJwtGenerator.getJwtPair(userId);
        return new UserOauthResponse(userId, jwtPair);
    }

    public void withDraw() {
        User user = userReader.getCurrentUser();
        user.withDraw();
        if (user.getSnsType() == SnsType.NAVER) {
            withDrawNaver(user);
        } else if (user.getSnsType() == SnsType.KAKAO) {
            withDrawKakao();
        }
    }

    public void withDrawNaver(User user) {
        String naverRefreshToken = user.getNaverRefreshToken();
        NaverTokenRefreshRequest refreshRequest = NaverTokenRefreshRequest.builder()
                .clientId(naverProperties.clientId())
                .clientSecret(naverProperties.clientSecret())
                .refreshToken(naverRefreshToken)
                .grantType("refresh_token")
                .build();
        NaverTokenResponse naverTokenResponse = naverOAuthClient.tokenRefresh(refreshRequest);

        String naverAccessToken = naverTokenResponse.accessToken();
        NaverTokenRevokeRequest deleteRequest = NaverTokenRevokeRequest.builder()
                .clientId(naverProperties.clientId())
                .clientSecret(naverProperties.clientSecret())
                .accessToken(naverAccessToken)
                .serviceProvide("NAVER")
                .grantType("delete")
                .build();
        naverOAuthClient.tokenRevoke(deleteRequest);
    }
}
