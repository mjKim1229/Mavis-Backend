package com.mavis.api.auth.facade;

import com.mavis.api.auth.dto.UserKakaoOauthResponse;
import com.mavis.api.auth.implement.UserJwtGenerator;
import com.mavis.api.auth.mapper.UserMapper;
import com.mavis.common.dto.JwtPair;
import com.mavis.common.properties.KakaoProperties;
import com.mavis.infrastructure.outer.api.oauth.client.KakaoInfoClient;
import com.mavis.infrastructure.outer.api.oauth.client.KakaoOAuthClient;
import com.mavis.infrastructure.outer.api.oauth.dto.KakaoOAuthRequest;
import com.mavis.infrastructure.outer.api.oauth.dto.KakaoTokenResponse;
import com.mavis.infrastructure.outer.api.oauth.dto.KakaoUserInfoResponse;
import com.mavis.infrastructure.outer.api.oauth.dto.UnlinkKaKaoTarget;
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
    private final KakaoProperties kakaoProperties;

    public UserKakaoOauthResponse register(String code) {
        KakaoOAuthRequest kakaoOAuthRequest = userMapper.fromCode(code);
        KakaoTokenResponse kakaoTokenResponse = kakaoOAuthClient.kakaoAuth(kakaoOAuthRequest);

        String bearerAccessToken = BEARER + kakaoTokenResponse.accessToken();
        KakaoUserInfoResponse userInfo = kakaoInfoClient.getUserInfo(bearerAccessToken);

        //TODO user 저장 service
        JwtPair jwtPair = userJwtGenerator.getJwtPair(userInfo.id());
        return new UserKakaoOauthResponse(userInfo.id(), jwtPair);
    }

    public void withDraw() {
    }

    private void unlinkKakao(Long snsId) {
        String header = "KakaoAK " + kakaoProperties.adminKey();
        UnlinkKaKaoTarget unlinkKakaoTarget = UnlinkKaKaoTarget.from(snsId);
        kakaoInfoClient.unlink(header, unlinkKakaoTarget);
    }
}
