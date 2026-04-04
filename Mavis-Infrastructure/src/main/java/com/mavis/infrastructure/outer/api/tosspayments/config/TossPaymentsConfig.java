package com.mavis.infrastructure.outer.api.tosspayments.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

public class TossPaymentsConfig {

    @Bean
    public ErrorDecoder tossPaymentsErrorDecoder(ObjectMapper objectMapper) {
        return new TossPaymentsErrorDecoder(objectMapper);
    }
}
