package com.mavis.api.order.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.mavis.api.support.ControllerTestSupport;
import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.domain.OrderAddress;
import com.mavis.domain.domains.order.repository.OrderRepository;
import com.mavis.domain.domains.user.domain.SnsType;
import com.mavis.domain.domains.user.domain.User;
import com.mavis.domain.domains.user.repository.UserRepository;
import com.mavis.infrastructure.outer.api.tosspayments.dto.ConfirmPaymentRequest;
import com.mavis.common.util.OrderNumberGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.MediaType;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureWireMock(port = 0)
class ConfirmPaymentIntegrationTest extends ControllerTestSupport {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void 결제_승인_성공() throws Exception {
        User user = userRepository.save(User.builder()
                .snsType(SnsType.KAKAO)
                .name("테스트유저")
                .build());

        String tossOrderId = OrderNumberGenerator.generateOrderId();
        orderRepository.save(Order.builder()
                .orderId(tossOrderId)
                .user(user)
                .totalPrice(14000)
                .orderAddress(new OrderAddress("홍길동", "010-1234-5678", "12345", "서울시 강남구", "101호", "문 앞에 놔주세요"))
                .build());

        WireMock.stubFor(WireMock.post(urlEqualTo("/v1/payments/confirm"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(tossConfirmResponse(tossOrderId, 14000))));

        String confirmBody = objectMapper.writeValueAsString(
                new ConfirmPaymentRequest("test-payment-key", tossOrderId, 14000));

        mockMvc.perform(post("/v1/api/order/toss/confirm")
                        .header("Authorization", userToken(user.getId()))
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(confirmBody))
                .andExpect(status().isOk());

        WireMock.verify(postRequestedFor(urlEqualTo("/v1/payments/confirm")));
    }

    @Test
    void Toss_API_실패시_500_반환() throws Exception {
        User user = userRepository.save(User.builder()
                .snsType(SnsType.KAKAO)
                .name("테스트유저2")
                .build());

        String tossOrderId = OrderNumberGenerator.generateOrderId();
        orderRepository.save(Order.builder()
                .orderId(tossOrderId)
                .user(user)
                .totalPrice(14000)
                .orderAddress(new OrderAddress("홍길동", "010-1234-5678", "12345", "서울시 강남구", "101호", "문 앞에 놔주세요"))
                .build());

        WireMock.stubFor(WireMock.post(urlEqualTo("/v1/payments/confirm"))
                .willReturn(aResponse().withStatus(500)));

        String confirmBody = objectMapper.writeValueAsString(
                new ConfirmPaymentRequest("test-payment-key", tossOrderId, 14000));

        mockMvc.perform(post("/v1/api/order/toss/confirm")
                        .header("Authorization", userToken(user.getId()))
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(confirmBody))
                .andExpect(status().is5xxServerError());
    }

    private String tossConfirmResponse(String orderId, int amount) {
        return """
                {
                    "paymentKey": "test-payment-key",
                    "orderId": "%s",
                    "orderName": "테스트상품 외 0건",
                    "totalAmount": %d,
                    "balanceAmount": %d,
                    "status": "DONE",
                    "method": "카드",
                    "requestedAt": "2024-01-01T10:00:00+09:00",
                    "approvedAt": "2024-01-01T10:00:01+09:00",
                    "isPartialCancelable": false
                }
                """.formatted(orderId, amount, amount);
    }
}
