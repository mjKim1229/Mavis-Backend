package com.mavis.admin.domains.refund.controller;

import com.mavis.admin.common.page.PageResponse;
import com.mavis.admin.domains.refund.dto.GetAdminRefundResponse;
import com.mavis.admin.domains.refund.facade.AdminRefundFacade;
import com.mavis.admin.domains.refund.service.AdminRefundService;
import com.mavis.domain.domains.refund.domain.RefundStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/api/refund")
@Tag(name = "관리자 환불 API")
public class AdminRefundController {

    private final AdminRefundService adminRefundService;
    private final AdminRefundFacade adminRefundFacade;

    @Operation(summary = "환불 목록 조회")
    @GetMapping
    public PageResponse<GetAdminRefundResponse> getRefundList(Pageable pageable, @RequestParam(required = false) RefundStatus refundStatus) {
        return adminRefundService.getRefundList(pageable, refundStatus);
    }

    @Operation(summary = "환불 승인 (반품 → Toss cancel)")
    @PostMapping("/{refundId}/approve")
    public void approveRefund(@PathVariable Long refundId) {
        adminRefundFacade.approveRefund(refundId);
    }

    @Operation(summary = "환불 거절")
    @PostMapping("/{refundId}/reject")
    public void rejectRefund(@PathVariable Long refundId) {
        adminRefundService.rejectRefund(refundId);
    }
}
