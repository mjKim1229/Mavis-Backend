package com.mavis.api.refund.controller;

import com.mavis.api.refund.dto.CreateRefundRequest;
import com.mavis.api.refund.facade.RefundFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/v1/api/refund")
@RequiredArgsConstructor
@RestController
@Tag(name = "환불 API")
public class RefundController {

    private final RefundFacade refundFacade;

    @Operation(summary = "배송 전 환불 요청")
    @PostMapping("/{orderItemId}/cancel")
    public void cancelRefund(@PathVariable Long orderItemId, @RequestBody CreateRefundRequest request) {
        refundFacade.cancelRefund(orderItemId, request);
    }

    @Operation(summary = "반품 신청 (배송 후)")
    @PostMapping("/{orderItemId}/return")
    public void requestReturn(@PathVariable Long orderItemId, @RequestBody CreateRefundRequest request) {
        refundFacade.requestReturn(orderItemId, request);
    }
}
