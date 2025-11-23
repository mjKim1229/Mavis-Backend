package com.mavis.admin.domains.order.controller;

import com.mavis.admin.common.page.PageResponse;
import com.mavis.admin.domains.order.dto.OrderInfo;
import com.mavis.admin.domains.order.service.AdminOrderService;
import com.mavis.domain.domains.order.domain.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/api/order")
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    @GetMapping
    public PageResponse<OrderInfo> getOrderInfoLists(Pageable pageable, @RequestParam OrderStatus orderStatus) {
        return adminOrderService.getOrderLists(pageable, orderStatus);
    }
}
