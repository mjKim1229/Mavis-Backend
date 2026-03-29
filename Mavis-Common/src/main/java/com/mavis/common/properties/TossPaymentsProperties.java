package com.mavis.common.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@ConfigurationProperties(prefix = "payments.toss")
public record TossPaymentsProperties(
        String clientKey,
        String secretKey
) {
    public String getAuthorizationHeader() {
        return "Basic " + Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
    }
}
