package com.mavis.admin.domains.order.controller;

import com.mavis.admin.common.page.PageResponse;
import com.mavis.admin.domains.order.dto.AdminOrderConfirmRequest;
import com.mavis.admin.domains.order.dto.GetAdminOrderExcelResponse;
import com.mavis.admin.domains.order.dto.OrderInfo;
import com.mavis.admin.domains.order.service.AdminOrderService;
import com.mavis.domain.domains.order.domain.OrderStatus;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/api/order")
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    @Operation(summary = "상태별 주문 목록 조회 (결제 완료, 발주 완료)")
    @GetMapping
    public PageResponse<GetAdminOrderExcelResponse> getOrderInfoLists(Pageable pageable, OrderStatus orderStatus) {
        return adminOrderService.getOrderLists(pageable, orderStatus);
    }

    @PostMapping("/confirm")
    @Operation(summary = "고객 발주 확인 (결제 완료 -> 발주 완료)")
    public void confirmOrder(@RequestBody AdminOrderConfirmRequest request) {
        adminOrderService.confirmOrder(request);
    }
}
