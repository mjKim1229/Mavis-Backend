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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class TossPaymentsErrorDecoder implements ErrorDecoder {

    private static final Map<String, BaseErrorCode> CANCEL_ERROR_CODE_MAP = new HashMap<>();
    private static final Map<String, BaseErrorCode> CONFIRM_ERROR_CODE_MAP = new HashMap<>();
    private final ObjectMapper objectMapper;

    static {
        Arrays.stream(PaymentsCancelErrorCode.values())
                .forEach(code -> CANCEL_ERROR_CODE_MAP.put(code.name(), code));
        Arrays.stream(PaymentsConfirmErrorCode.values())
                .forEach(code -> CONFIRM_ERROR_CODE_MAP.put(code.name(), code));
    }

    @Override
    public Exception decode(String methodKey, Response response) {
        try {
            byte[] bodyBytes = response.body().asInputStream().readAllBytes();
            String body = new String(bodyBytes, StandardCharsets.UTF_8);
            TossErrorResponse errorResponse = objectMapper.readValue(body, TossErrorResponse.class);
            String tossCode = errorResponse.code();
            Map<String, BaseErrorCode> errorCodeMap = methodKey.contains("Cancel")
                    ? CANCEL_ERROR_CODE_MAP
                    : CONFIRM_ERROR_CODE_MAP;
            BaseErrorCode errorCode = errorCodeMap.getOrDefault(tossCode, GlobalErrorCode.INTERNAL_SERVER_ERROR);
            return new TossPaymentsException(errorCode);
        } catch (Exception e) {
            log.error("Toss 에러 응답 파싱 실패", e);
            return new TossPaymentsException(GlobalErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
