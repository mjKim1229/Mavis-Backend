package com.mavis.infrastructure.outer.api.tosspayments.client;

import com.mavis.infrastructure.outer.api.tosspayments.config.TossPaymentsConfig;
import com.mavis.infrastructure.outer.api.tosspayments.dto.PaymentsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "PaymentsQueryByOrderClient",
        url = "https://api.tosspayments.com",
        configuration = TossPaymentsConfig.class
)
public interface PaymentsQueryByOrderClient {

    @GetMapping("/v1/payments/orders/{orderId}")
    PaymentsResponse getPaymentByOrderId(
            @RequestHeader(name = "Authorization") String authorization,
            @PathVariable String orderId
    );
}
