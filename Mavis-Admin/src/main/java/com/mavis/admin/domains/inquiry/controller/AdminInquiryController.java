package com.mavis.admin.domains.inquiry.controller;

import com.mavis.admin.common.page.PageResponse;
import com.mavis.admin.domains.inquiry.dto.CreateInquiryAnswerRequest;
import com.mavis.admin.domains.inquiry.dto.GetAdminInquiryDetailResponse;
import com.mavis.admin.domains.inquiry.dto.GetAdminInquiryResponse;
import com.mavis.admin.domains.inquiry.dto.UpdateInquiryAnswerRequest;
import com.mavis.admin.domains.inquiry.service.AdminInquiryService;
import com.mavis.admin.domains.inquiry.service.InquiryAnswerService;
import com.mavis.domain.domains.inquiry.domain.AnswerStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/api/inquiry")
@Tag(name = "관리자 문의 API")
public class AdminInquiryController {

    private final InquiryAnswerService inquiryAnswerService;
    private final AdminInquiryService adminInquiryService;

    @GetMapping("/{inquiryId}")
    @Operation(summary = "문의 상세 조회")
    public GetAdminInquiryDetailResponse getInquiry(@PathVariable Long inquiryId) {
        return adminInquiryService.getInquiry(inquiryId);
    }

    @GetMapping
    @Operation(summary = "문의 목록 조회")
    public PageResponse<GetAdminInquiryResponse> getInquiries(
            @RequestParam(defaultValue = "ALL") AnswerStatus status,
            Pageable pageable
    ) {
        return adminInquiryService.getInquiries(status, pageable);
    }

    @PostMapping("/answer/{inquiryId}")
    @Operation(summary = "문의 답변 등록")
    public void createInquiryAnswer(@PathVariable Long inquiryId, @RequestBody CreateInquiryAnswerRequest request) {
        inquiryAnswerService.createInquiryAnswer(inquiryId, request.answer());
    }

    @PutMapping("/answer/{inquiryId}")
    @Operation(summary = "문의 답변 수정")
    public void updateInquiryAnswer(@PathVariable Long inquiryId, @RequestBody UpdateInquiryAnswerRequest request) {
        inquiryAnswerService.updateInquiryAnswer(inquiryId, request.answer());
    }

    @DeleteMapping("/answer/{inquiryId}")
    @Operation(summary = "문의 답변 삭제")
    public void deleteInquiryAnswer(@PathVariable Long inquiryId) {
        inquiryAnswerService.deleteInquiryAnswer(inquiryId);
    }
}
