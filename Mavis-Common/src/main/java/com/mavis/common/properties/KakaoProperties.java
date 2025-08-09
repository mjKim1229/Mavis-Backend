package com.mavis.common.properties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "auth.oauth.kakao")
public class KakaoProperties {
    private final String clientId;
    private final String redirectUrl;
    private final String adminKey;
}
