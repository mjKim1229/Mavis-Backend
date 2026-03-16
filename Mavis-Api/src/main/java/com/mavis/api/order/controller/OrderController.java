package com.mavis.api.order.controller;

import com.mavis.api.common.page.PageResponse;
import com.mavis.api.order.dto.CreateOrderRequest;
import com.mavis.api.order.dto.CreateOrderResponse;
import com.mavis.api.order.dto.GetOrderItemUserCanReviewResponse;
import com.mavis.api.order.dto.UserOrderInfo;
import com.mavis.api.order.facade.OrderFacade;
import com.mavis.api.order.service.OrderService;
import com.mavis.infrastructure.outer.api.tosspayments.dto.CancelPaymentsRequest;
import com.mavis.infrastructure.outer.api.tosspayments.dto.ConfirmPaymentRequest;
import com.mavis.infrastructure.outer.api.tosspayments.dto.VirtualAccountDepositCallbackRequest;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/v1/api/order")
@RequiredArgsConstructor
@RestController
public class OrderController {

    private final OrderService orderService;
    private final OrderFacade orderFacade;

    @Operation(summary = "주문 생성")
    @PostMapping
    public CreateOrderResponse createOrder(@RequestBody CreateOrderRequest request) {
        return orderService.createOrder(request);
    }

    @Operation(summary = "회원별 주문 & 배송 목록 조회")
    @GetMapping
    public PageResponse<UserOrderInfo> getUserOrderList(Pageable pageable) {
        return orderService.getUserOrderList(pageable);
    }

    @Operation(summary = "리뷰 가능한 주문 & 배송 목록")
    @GetMapping("/reviewable")
    public PageResponse<GetOrderItemUserCanReviewResponse> getOrderItemUserCanReviewResponsePageResponse(Pageable pageable) {
        return orderService.getUserCanReviewList(pageable);
    }

    @Operation(summary = "토스 PG 결제 승인")
    @PostMapping("/toss/confirm")
    public void confirmPayments(@RequestBody ConfirmPaymentRequest request) {
        orderFacade.confirmPayments(request);
    }

    @Operation(summary = "토스 PG 주문 취소 (환불)")
    @PostMapping("/{orderId}/toss/cancel")
    public void cancelPayments(@PathVariable Long orderId, @RequestBody CancelPaymentsRequest cancelPaymentsRequest) {
        orderFacade.cancelPayments(orderId, cancelPaymentsRequest);
    }

    @Operation(summary = "토스 PG 가상계좌 입금 콜백")
    @PostMapping("/deposit-callback")
    public void depositCallback(@RequestBody VirtualAccountDepositCallbackRequest request) {
        orderService.processDepositCallback(request);
    }
}
