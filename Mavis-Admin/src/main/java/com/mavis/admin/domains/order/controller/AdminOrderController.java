package com.mavis.admin.domains.order.controller;

import com.mavis.admin.common.page.PageResponse;
import com.mavis.admin.domains.order.dto.AdminOrderConfirmRequest;
import com.mavis.admin.domains.order.dto.AdminOrderCountResponse;
import com.mavis.admin.domains.order.dto.GetAdminOrderResponse;
import com.mavis.admin.domains.order.service.AdminOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/api/order")
@Tag(name = "관리자 주문 API")
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    @Operation(summary = "주문 상태별 카운트 조회 (결제 완료, 발주 완료, 배송중, 배송 완료)")
    @GetMapping("/counts")
    public AdminOrderCountResponse getOrderCounts() {
        return adminOrderService.getOrderCounts();
    }

    @Operation(summary = "결제 완료 주문 목록 조회")
    @GetMapping("/payment-confirmed")
    public PageResponse<GetAdminOrderResponse> getPaymentConfirmedOrderLists(Pageable pageable) {
        return adminOrderService.getPaymentConfirmedOrderLists(pageable);
    }

    @Operation(summary = "발주 완료 주문 목록 조회 (기간 필터)")
    @GetMapping("/ordered")
    public PageResponse<GetAdminOrderResponse> getOrderedOrderLists(
            Pageable pageable,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return adminOrderService.getOrderedOrderLists(pageable, startDate, endDate);
    }

    @PostMapping("/confirm")
    @Operation(summary = "고객 발주 확인 (결제 완료 -> 발주 완료)")
    public void confirmOrder(@RequestBody AdminOrderConfirmRequest request) {
        adminOrderService.confirmOrder(request);
    }
}
