package com.mavis.infrastructure.outer.api.oauth.client.naver;

import com.mavis.infrastructure.outer.api.oauth.config.FeignConfig;
import com.mavis.infrastructure.outer.api.oauth.dto.NaverUserInfoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "NaverInfoClient",
        configuration = FeignConfig.class,
        url = "https://openapi.naver.com"
)
public interface NaverInfoClient {
    @GetMapping("/v1/nid/me")
    NaverUserInfoResponse getUserInfo(@RequestHeader("Authorization") String accessToken);
}
