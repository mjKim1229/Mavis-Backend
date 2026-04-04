package com.mavis.infrastructure.outer.api.tosspayments.client;

import com.mavis.infrastructure.outer.api.tosspayments.config.TossPaymentsConfig;
import com.mavis.infrastructure.outer.api.tosspayments.dto.PaymentsResponse;
import com.mavis.infrastructure.outer.api.tosspayments.dto.TossConfirmRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "PaymentsConfirmClient",
        url = "https://api.tosspayments.com",
        configuration = TossPaymentsConfig.class
)
public interface PaymentsConfirmClient {
    @PostMapping("/v1/payments/confirm")
    PaymentsResponse confirmPayments(
            @RequestHeader(name = "Authorization") String authorization,
            @RequestHeader(name = "Idempotency-Key") String idempotencyKey,
            @RequestHeader(name = "TossPayments-Test-Code", required = false) String testCode,
            @RequestBody TossConfirmRequest request
    );
}
