package com.mavis.api.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mavis.api.order.dto.CreateOrderRequest;
import com.mavis.api.order.dto.CreateOrderResponse;
import com.mavis.api.order.dto.OrderAddressRequest;
import com.mavis.api.order.dto.OrderItemRequest;
import com.mavis.api.order.facade.OrderFacade;
import com.mavis.api.order.service.OrderService;
import com.mavis.domain.domains.order.domain.OrderOption;
import com.mavis.infrastructure.outer.discord.DiscordNotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    private static final String ORDER_URL = "/v1/api/order";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private OrderFacade orderFacade;

    @MockitoBean
    private DiscordNotificationService discordNotificationService;

    private OrderAddressRequest validAddress() {
        return new OrderAddressRequest("홍길동", "01012345678", "12345", "서울시 강남구", "101호", null);
    }

    private List<OrderItemRequest> validItems() {
        OrderOption option = new OrderOption("black", 1);
        OrderItemRequest item = new OrderItemRequest(1L, option);
        return List.of(item);
    }

    private ResultActions postOrder(Object request) throws Exception {
        String body = objectMapper.writeValueAsString(request);
        return mockMvc.perform(post(ORDER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private void assertValidationFailed(ResultActions result) throws Exception {
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_400_1"));
        verify(orderService, never()).createOrder(any());
    }

    @Test
    void 배송메모가_null이어도_주문_생성_성공() throws Exception {
        CreateOrderResponse response = CreateOrderResponse.from("GARAM-TEST");
        given(orderService.createOrder(any())).willReturn(response);
        CreateOrderRequest request = new CreateOrderRequest(14000, validAddress(), validItems());

        postOrder(request).andExpect(status().isOk());

        verify(orderService, times(1)).createOrder(any());
    }

    @Test
    void 주문상품이_비어있으면_400() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest(4000, validAddress(), List.of());

        assertValidationFailed(postOrder(request));
    }

    @Test
    void 주문상품이_null이면_400() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest(4000, validAddress(), null);

        assertValidationFailed(postOrder(request));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, Integer.MIN_VALUE})
    void 결제금액이_0_이하면_400(int amount) throws Exception {
        CreateOrderRequest request = new CreateOrderRequest(amount, validAddress(), validItems());

        assertValidationFailed(postOrder(request));
    }

    @Test
    void 배송지가_없으면_400() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest(14000, null, validItems());

        assertValidationFailed(postOrder(request));
    }

    @Test
    void 받는분_이름이_공백이면_400() throws Exception {
        OrderAddressRequest address = new OrderAddressRequest("  ", "01012345678", "12345", "서울시 강남구", "101호", null);
        CreateOrderRequest request = new CreateOrderRequest(14000, address, validItems());

        assertValidationFailed(postOrder(request));
    }

    @Test
    void 상세주소가_비어있으면_400() throws Exception {
        OrderAddressRequest address = new OrderAddressRequest("홍길동", "01012345678", "12345", "서울시 강남구", "", null);
        CreateOrderRequest request = new CreateOrderRequest(14000, address, validItems());

        assertValidationFailed(postOrder(request));
    }

    @Test
    void 연락처_우편번호_주소가_null이면_400() throws Exception {
        OrderAddressRequest address = new OrderAddressRequest("홍길동", null, null, null, "101호", null);
        CreateOrderRequest request = new CreateOrderRequest(14000, address, validItems());

        assertValidationFailed(postOrder(request));
    }
}
