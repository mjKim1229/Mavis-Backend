package com.mavis.api.order.controller;

import com.mavis.api.order.dto.CreateOrderRequest;
import com.mavis.api.order.dto.UserOrderInfo;
import com.mavis.api.order.service.OrderService;
import com.mavis.domain.domains.order.domain.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/v1/api/order")
@RequiredArgsConstructor
@RestController
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public void createOrder(@RequestBody CreateOrderRequest request) {
        orderService.createOrder(request);
    }

    @GetMapping
    public List<UserOrderInfo> getUserOrderList(Pageable pageable, OrderStatus orderStatus) {
        return orderService.getOrderList(pageable, orderStatus);
    }
}
