package com.mavis.infrastructure.outer.api.tosspayments.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mavis.common.exception.BaseErrorCode;
import com.mavis.common.exception.GlobalErrorCode;
import com.mavis.infrastructure.outer.api.tosspayments.dto.TossErrorResponse;
import com.mavis.infrastructure.outer.api.tosspayments.exception.PaymentsCancelErrorCode;
import com.mavis.infrastructure.outer.api.tosspayments.exception.PaymentsConfirmErrorCode;
import com.mavis.infrastructure.outer.api.tosspayments.exception.TossPaymentsException;
import feign.Response;
import feign.codec.ErrorDecoder;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class TossPaymentsErrorDecoder implements ErrorDecoder {

    private static final Map<String, BaseErrorCode> ERROR_CODE_MAP = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    static {
        // cancel 먼저 등록 후 confirm이 중복 키를 덮어씀 (INVALID_REQUEST, PROVIDER_ERROR)
        Arrays.stream(PaymentsCancelErrorCode.values())
                .forEach(code -> ERROR_CODE_MAP.put(code.name(), code));
        Arrays.stream(PaymentsConfirmErrorCode.values())
                .forEach(code -> ERROR_CODE_MAP.put(code.name(), code));
    }

    @Override
    public Exception decode(String methodKey, Response response) {
        try {
            byte[] bodyBytes = response.body().asInputStream().readAllBytes();
            String body = new String(bodyBytes, StandardCharsets.UTF_8);
            TossErrorResponse errorResponse = objectMapper.readValue(body, TossErrorResponse.class);
            String tossCode = errorResponse.error().code();
            BaseErrorCode errorCode = ERROR_CODE_MAP.getOrDefault(tossCode, GlobalErrorCode.INTERNAL_SERVER_ERROR);
            return new TossPaymentsException(errorCode);
        } catch (Exception e) {
            return new TossPaymentsException(GlobalErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
