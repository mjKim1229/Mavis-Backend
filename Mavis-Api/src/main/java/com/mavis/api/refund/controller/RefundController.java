package com.mavis.api.refund.controller;

import com.mavis.api.refund.dto.CreateRefundRequest;
import com.mavis.api.refund.dto.RequestReturnRequest;
import com.mavis.api.refund.facade.RefundFacade;
import com.mavis.api.refund.service.RefundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequestMapping("/v1/api/refund")
@RequiredArgsConstructor
@RestController
@Tag(name = "환불 API")
public class RefundController {

    private final RefundFacade refundFacade;
    private final RefundService refundService;

    @Operation(summary = "배송 전 환불 요청")
    @PostMapping("/{orderItemId}/cancel")
    public void cancelRefund(@PathVariable Long orderItemId, @RequestBody CreateRefundRequest request) {
        refundFacade.cancelRefund(orderItemId, request);
    }

    @Operation(summary = "반품 신청 (배송 후)")
    @PostMapping(value = "/{orderItemId}/return", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void requestReturn(@PathVariable Long orderItemId,
                              @RequestPart RequestReturnRequest request,
                              @RequestPart List<MultipartFile> images) {
        refundService.createReturnRefund(orderItemId, request, images);
    }
}
