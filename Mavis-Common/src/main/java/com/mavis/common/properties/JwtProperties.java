package com.mavis.common.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("auth.jwt")
public record JwtProperties(String secretKey, Long accessExp, Long refreshExp) {
}
