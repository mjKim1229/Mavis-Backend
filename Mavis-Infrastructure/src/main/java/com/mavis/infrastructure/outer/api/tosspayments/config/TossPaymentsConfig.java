package com.mavis.infrastructure.outer.api.tosspayments.config;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

public class TossPaymentsConfig {

    @Bean
    public ErrorDecoder tossPaymentsErrorDecoder() {
        return new TossPaymentsErrorDecoder();
    }
}
