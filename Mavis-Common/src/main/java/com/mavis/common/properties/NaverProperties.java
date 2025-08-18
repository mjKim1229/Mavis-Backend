package com.mavis.common.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth.oauth.naver")
public record NaverProperties(
        String clientId,
        String clientSecret,
        String redirectUri,
        String state
) {
}
