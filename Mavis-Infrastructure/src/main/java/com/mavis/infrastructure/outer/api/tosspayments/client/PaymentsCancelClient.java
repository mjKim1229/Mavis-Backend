package com.mavis.infrastructure.outer.api.tosspayments.client;

import com.mavis.infrastructure.outer.api.oauth.config.FeignConfig;
import com.mavis.infrastructure.outer.api.tosspayments.dto.CancelPaymentsRequest;
import com.mavis.infrastructure.outer.api.tosspayments.dto.PaymentsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "PaymentsCancelClient",
        url = "https://api.tosspayments.com",
        configuration = FeignConfig.class
)
public interface PaymentsCancelClient {
    @PostMapping("/v1/payments/{paymentKey}/cancel")
    PaymentsResponse cancelPayments(
            @RequestHeader(name = "Authorization") String authorization,
            @PathVariable("paymentKey") String paymentKey,
            @RequestBody CancelPaymentsRequest cancelPaymentsRequest);
}
