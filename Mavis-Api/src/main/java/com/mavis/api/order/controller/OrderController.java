package com.mavis.api.order.controller;

import com.mavis.api.common.page.PageResponse;
import com.mavis.api.order.dto.CreateOrderRequest;
import com.mavis.api.order.dto.OrderAddressResponse;
import com.mavis.api.order.dto.PendingOrderRequest;
import com.mavis.api.order.dto.UserOrderInfo;
import com.mavis.api.order.service.OrderService;
import com.mavis.infrastructure.outer.api.tosspayments.dto.ConfirmPaymentRequest;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/v1/api/order")
@RequiredArgsConstructor
@RestController
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "주문 생성")
    @PostMapping
    public void createOrder(@RequestBody CreateOrderRequest request) {
        orderService.createOrder(request);
    }

    @Operation(summary = "회원별 주문 & 배송 목록 조회")
    @GetMapping
    public PageResponse<UserOrderInfo> getUserOrderList(Pageable pageable) {
        return orderService.getUserOrderList(pageable);
    }

//    @PostMapping("/toss/pending")
//    public void createPendingOrder(@RequestBody PendingOrderRequest request) {
//        orderService.createPendingOrder(request);
//    }

//    @PostMapping("/toss/pending/verify")
//    public void verifyPendingOrder(@RequestBody PendingOrderRequest request) {
//        orderService.validatePendingOrder(request);
//    }

    @PostMapping("/toss/confirm/test")
    public void confirmPayments(@RequestBody ConfirmPaymentRequest request) {
        orderService.confirmPayments(request);
    }

    @GetMapping("/address")
    @Operation(summary = "사용자 배송지 조회")
    public OrderAddressResponse getUserAddress() {
        return orderService.getUserAddress();
    }
}
