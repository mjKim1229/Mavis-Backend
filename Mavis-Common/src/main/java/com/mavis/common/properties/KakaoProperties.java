package com.mavis.common.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth.oauth.kakao")
public record KakaoProperties(String clientId, String redirectUrl, String adminKey) {
}
