package com.mavis.api.order.controller;

import com.mavis.api.order.dto.CreateOrderRequest;
import com.mavis.api.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/v1/api/order")
@RequiredArgsConstructor
@RestController
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public void createOrder(@RequestBody List<CreateOrderRequest> request) {
        orderService.createOrder(request);
    }
}
