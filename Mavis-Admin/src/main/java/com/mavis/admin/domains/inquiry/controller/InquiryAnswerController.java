package com.mavis.admin.domains.inquiry.controller;

import com.mavis.admin.domains.inquiry.dto.CreateInquiryAnswerRequest;
import com.mavis.admin.domains.inquiry.dto.UpdateInquiryAnswerRequest;
import com.mavis.admin.domains.inquiry.service.InquiryAnswerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/api/inquiry-answer")
@Tag(name = "관리자 문의 답변 API")
public class InquiryAnswerController {

    private final InquiryAnswerService inquiryAnswerService;

    @PostMapping("/{inquiryId}")
    @Operation(summary = "문의 답변 등록")
    public void createInquiryAnswer(@PathVariable Long inquiryId, @RequestBody CreateInquiryAnswerRequest request) {
        inquiryAnswerService.createInquiryAnswer(inquiryId, request.answer());
    }

    @PutMapping("/{inquiryId}")
    @Operation(summary = "문의 답변 수정")
    public void updateInquiryAnswer(@PathVariable Long inquiryId, @RequestBody UpdateInquiryAnswerRequest request) {
        inquiryAnswerService.updateInquiryAnswer(inquiryId, request.answer());
    }

    @DeleteMapping("/{inquiryId}")
    @Operation(summary = "문의 답변 삭제")
    public void deleteInquiryAnswer(@PathVariable Long inquiryId) {
        inquiryAnswerService.deleteInquiryAnswer(inquiryId);
    }
}
