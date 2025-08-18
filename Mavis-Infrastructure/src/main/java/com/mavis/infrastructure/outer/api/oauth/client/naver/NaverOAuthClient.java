package com.mavis.infrastructure.outer.api.oauth.client.naver;

import com.mavis.infrastructure.outer.api.oauth.config.FeignConfig;
import com.mavis.infrastructure.outer.api.oauth.dto.NaverOAuthRequest;
import com.mavis.infrastructure.outer.api.oauth.dto.NaverTokenResponse;
import com.mavis.infrastructure.outer.api.oauth.dto.NaverTokenRevokeRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(
        name = "NaverOAuthClient",
        configuration = FeignConfig.class,
        url = "https://nid.naver.com"
)
public interface NaverOAuthClient {
    @PostMapping(value = "/oauth2.0/token?grant_type=authorization_code", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    NaverTokenResponse naverAuth(NaverOAuthRequest request);

    @PostMapping(value = "/oauth2.0/token")
    void tokenRevoke(NaverTokenRevokeRequest request);

    @PostMapping(value = "/oauth2.0/token")
    void refreshToken();
}
