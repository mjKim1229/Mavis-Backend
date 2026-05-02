package com.mavis.api.auth.facade;

import com.mavis.api.auth.dto.OauthLoginRequest;
import com.mavis.api.auth.dto.UserOauthResponse;
import com.mavis.api.auth.implement.UserJwtGenerator;
import com.mavis.api.auth.implement.UserReader;
import com.mavis.api.auth.mapper.OAuthMapper;
import com.mavis.api.auth.service.UserService;
import com.mavis.common.dto.JwtPair;
import com.mavis.domain.domains.user.domain.SnsType;
import com.mavis.domain.domains.user.domain.User;
import com.mavis.infrastructure.outer.api.oauth.KakaoAgreementService;
import com.mavis.infrastructure.outer.api.oauth.NaverAgreementService;
import com.mavis.infrastructure.outer.api.oauth.client.kakao.KakaoInfoClient;
import com.mavis.infrastructure.outer.api.oauth.client.kakao.KakaoOAuthClient;
import com.mavis.infrastructure.outer.api.oauth.client.naver.NaverInfoClient;
import com.mavis.infrastructure.outer.api.oauth.client.naver.NaverOAuthClient;
import com.mavis.infrastructure.outer.api.oauth.dto.*;
import com.mavis.infrastructure.outer.api.oauth.dto.KakaoServiceTermsResponse.KakaoServiceTerm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.mavis.common.consts.MavisStatic.BEARER;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserFacade {
    private final KakaoOAuthClient kakaoOAuthClient;
    private final KakaoInfoClient kakaoInfoClient;
    private final KakaoAgreementService kakaoAgreementService;
    private final NaverOAuthClient naverOAuthClient;
    private final OAuthMapper oAuthMapper;
    private final UserJwtGenerator userJwtGenerator;
    private final NaverInfoClient naverInfoClient;
    private final NaverAgreementService naverAgreementService;
    private final UserService userService;
    private final UserReader userReader;

    public UserOauthResponse register(OauthLoginRequest request, String url) {
        KakaoOAuthRequest kakaoOAuthRequest = oAuthMapper.kakaoAuthRequest(request.code(), url);
        KakaoTokenResponse kakaoTokenResponse = kakaoOAuthClient.kakaoAuth(kakaoOAuthRequest);
        String bearerAccessToken = BEARER + kakaoTokenResponse.accessToken();

        KakaoUserInfoResponse userInfo = kakaoInfoClient.getUserInfo(bearerAccessToken);
        List<KakaoServiceTerm> serviceTerms = kakaoAgreementService.getAgreements(bearerAccessToken);
        boolean isEmailAgreed = serviceTerms.stream()
                .anyMatch(t -> "marketing_email".equals(t.tag()) && Boolean.TRUE.equals(t.agreed()));
        boolean isSmsAgreed = serviceTerms.stream()
                .anyMatch(t -> "marketing_sms".equals(t.tag()) && Boolean.TRUE.equals(t.agreed()));

        Long userId = userService.upsertKakaouser(userInfo, isEmailAgreed, isSmsAgreed);
        JwtPair jwtPair = userJwtGenerator.getJwtPair(userId);
        return new UserOauthResponse(userId, jwtPair);
    }

    private void unlinkKakao(Long snsId) {
        String adminHeader = oAuthMapper.kakaoAdminHeader();
        UnlinkKaKaoTarget target = UnlinkKaKaoTarget.from(snsId);
        kakaoInfoClient.unlink(adminHeader, target);
    }

    public UserOauthResponse registerNaver(OauthLoginRequest request) {
        NaverOAuthRequest naverOAuthRequest = oAuthMapper.naverAuthRequest(request.code());
        NaverTokenResponse naverTokenResponse = naverOAuthClient.naverAuth(naverOAuthRequest);
        String bearerAccessToken = BEARER + naverTokenResponse.accessToken();

        NaverUserInfoResponse userInfo = naverInfoClient.getUserInfo(bearerAccessToken);
        List<NaverAgreementInfo> agreements = naverAgreementService.getAgreements(bearerAccessToken);
        boolean isEmailAgreed = agreements.stream().anyMatch(info -> "marketing_email".equals(info.termCode()));
        boolean isSmsAgreed = agreements.stream().anyMatch(info -> "marketing_sms".equals(info.termCode()));

        Long userId = userService.upsertNaverUser(userInfo.response(), naverTokenResponse.refreshToken(), isEmailAgreed, isSmsAgreed);
        JwtPair jwtPair = userJwtGenerator.getJwtPair(userId);
        return new UserOauthResponse(userId, jwtPair);
    }

    public void withDraw() {
        User user = userReader.getCurrentUser();
        if (user.getSnsType() == SnsType.NAVER) {
            withDrawNaver(user);
        } else if (user.getSnsType() == SnsType.KAKAO) {
            Long snsId = Long.valueOf(user.getSnsId());
            unlinkKakao(snsId);
        }
        userService.withDraw();
    }

    public void withDrawNaver(User user) {
        NaverOAuthRequest refreshRequest = oAuthMapper.naverRefreshTokenRequest(user.getNaverRefreshToken());
        NaverTokenResponse naverTokenResponse = naverOAuthClient.naverAuth(refreshRequest);
        NaverOAuthRequest deleteRequest = oAuthMapper.naverDeleteRequest(naverTokenResponse.accessToken());
        naverOAuthClient.naverAuth(deleteRequest);
    }
}
