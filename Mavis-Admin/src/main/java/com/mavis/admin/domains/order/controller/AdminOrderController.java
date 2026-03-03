package com.mavis.admin.domains.order.controller;

import com.mavis.admin.common.page.PageResponse;
import com.mavis.admin.domains.order.dto.AdminOrderConfirmRequest;
import com.mavis.admin.domains.order.dto.OrderInfo;
import com.mavis.admin.domains.order.service.AdminOrderService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/api/order")
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    @Operation(summary = "고객 주문 목록 (아직 미발주 상태)")
    @GetMapping
    public PageResponse<OrderInfo> getOrderInfoLists(Pageable pageable) {
        return adminOrderService.getOrderLists(pageable);
    }

    @PostMapping("/confirm")
    @Operation(summary = "고객 발주 확인")
    public void confirmOrder(@RequestBody AdminOrderConfirmRequest request) {
        adminOrderService.confirmOrder(request);
    }
}
