package com.mavis.api.refund.controller;

import com.mavis.api.refund.dto.GetReturnPreviewRequest;
import com.mavis.api.refund.dto.GetReturnPreviewResponse;
import com.mavis.api.refund.dto.RequestReturnRequest;
import com.mavis.api.refund.service.RefundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

    private final RefundService refundService;

    @Operation(summary = "반품 환불 예상 금액 조회 (배송비 차감 내역 포함)")
    @GetMapping("/return/preview")
    public GetReturnPreviewResponse previewReturn(@Valid GetReturnPreviewRequest request) {
        return refundService.previewReturn(request);
    }

    @Operation(summary = "반품 신청 (배송 후, 한 주문의 여러 상품 묶음 신청)")
    @PostMapping(value = "/return", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void requestReturn(@RequestPart @Valid RequestReturnRequest request,
                              @RequestPart List<MultipartFile> images) {
        refundService.createReturnRefund(request, images);
    }
}
