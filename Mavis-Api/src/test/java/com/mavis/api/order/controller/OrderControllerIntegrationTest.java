package com.mavis.api.order.controller;

import com.mavis.api.order.dto.CreateOrderRequest;
import com.mavis.api.order.dto.OrderAddressRequest;
import com.mavis.api.order.dto.OrderItemRequest;
import com.mavis.api.support.ControllerTestSupport;
import com.mavis.common.enums.ProductSubCategory;
import com.mavis.domain.domains.order.domain.OrderOption;
import com.mavis.domain.domains.product.domain.Product;
import com.mavis.domain.domains.product.repository.ProductRepository;
import com.mavis.domain.domains.user.domain.SnsType;
import com.mavis.domain.domains.user.domain.User;
import com.mavis.domain.domains.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderControllerIntegrationTest extends ControllerTestSupport {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void 주문_생성_성공() throws Exception {
        User user = userRepository.save(User.builder()
                .snsType(SnsType.KAKAO)
                .name("테스트유저")
                .build());

        Product product = productRepository.save(Product.builder()
                .name("테스트상품")
                .price(10000)
                .subCategory(ProductSubCategory.TENCEL)
                .build());

        CreateOrderRequest request = new CreateOrderRequest(
                14000,
                new OrderAddressRequest("홍길동", "010-1234-5678", "12345", "서울시 강남구", "101호", "문 앞에 놔주세요"),
                List.of(new OrderItemRequest(product.getId(), new OrderOption("black", 1)))
        );

        mockMvc.perform(post("/v1/api/order")
                        .header("Authorization", userToken(user.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tossOrderId").exists());
    }

    @Test
    void 주문_금액_불일치시_400_반환() throws Exception {
        User user = userRepository.save(User.builder()
                .snsType(SnsType.KAKAO)
                .name("테스트유저")
                .build());

        Product product = productRepository.save(Product.builder()
                .name("테스트상품")
                .price(10000)
                .subCategory(ProductSubCategory.TENCEL)
                .build());

        CreateOrderRequest request = new CreateOrderRequest(
                99999, // 틀린 금액
                new OrderAddressRequest("홍길동", "010-1234-5678", "12345", "서울시 강남구", "101호", "문 앞에 놔주세요"),
                List.of(new OrderItemRequest(product.getId(), new OrderOption("black", 1)))
        );

        mockMvc.perform(post("/v1/api/order")
                        .header("Authorization", userToken(user.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 인증_없이_주문_생성시_401_반환() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest(
                14000,
                new OrderAddressRequest("홍길동", "010-1234-5678", "12345", "서울시 강남구", "101호", "문 앞에 놔주세요"),
                List.of()
        );

        mockMvc.perform(post("/v1/api/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
