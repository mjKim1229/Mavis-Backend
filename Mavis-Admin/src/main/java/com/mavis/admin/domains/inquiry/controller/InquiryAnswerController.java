package com.mavis.admin.domains.inquiry.controller;

import com.mavis.admin.domains.inquiry.dto.CreateInquiryAnswerRequest;
import com.mavis.admin.domains.inquiry.service.InquiryAnswerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/api/inquiry-answer")
public class InquiryAnswerController {

    private final InquiryAnswerService inquiryAnswerService;

    @PostMapping("/{inquiryId}")
    public void createInquiryAnswer(@PathVariable Long inquiryId, @RequestBody CreateInquiryAnswerRequest request) {
        inquiryAnswerService.createInquiryAnswer(inquiryId, request.answer());
    }
}
