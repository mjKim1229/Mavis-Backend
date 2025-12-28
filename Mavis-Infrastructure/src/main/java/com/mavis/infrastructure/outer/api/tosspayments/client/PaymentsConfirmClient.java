package com.mavis.infrastructure.outer.api.tosspayments.client;

import com.mavis.infrastructure.outer.api.oauth.config.FeignConfig;
import com.mavis.infrastructure.outer.api.tosspayments.dto.ConfirmPaymentRequest;
import com.mavis.infrastructure.outer.api.tosspayments.dto.PaymentsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "PaymentsConfirmClient",
        url = "https://api.tosspayments.com",
        configuration = FeignConfig.class
)
public interface PaymentsConfirmClient {
    @PostMapping("/v1/payments/confirm")
    PaymentsResponse confirmPayments(@RequestHeader(name = "Authorization") String authorization, @RequestBody ConfirmPaymentRequest request);
}
