package com.mavis.infrastructure.outer.api.oauth.client;

import com.mavis.infrastructure.outer.api.oauth.dto.KakaoOAuthRequest;
import com.mavis.infrastructure.outer.api.oauth.dto.KakaoTokenResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(
        name = "KakaoAuthClient",
        url = "https://kauth.kakao.com"
)
public interface KakaoOAuthClient {
    @PostMapping(value = "/oauth/token?grant_type=authorization_code", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    KakaoTokenResponse kakaoAuth(KakaoOAuthRequest kakaoOAuthRequest);
}
