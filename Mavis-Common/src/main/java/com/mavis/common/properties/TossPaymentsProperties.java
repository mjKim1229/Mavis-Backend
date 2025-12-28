package com.mavis.common.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "payments.toss")
public record TossPaymentsProperties(
        String clientKey,
        String secretKey
) {
}
