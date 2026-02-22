package com.mavis.api.inquiry.controller;

import com.mavis.api.common.page.PageResponse;
import com.mavis.api.inquiry.dto.CreateInquiryRequest;
import com.mavis.api.inquiry.dto.GetProductInquiryResponse;
import com.mavis.api.inquiry.dto.GetUserInquiryResponse;
import com.mavis.api.inquiry.service.InquiryService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/v1/api/inquiry")
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;

    @Operation(summary = "상품 문의 목록 조회")
    @GetMapping("/product/{id}")
    public PageResponse<GetProductInquiryResponse> getProductInquiries(@PathVariable Long id, @ParameterObject Pageable pageable) {
        return inquiryService.getProductInquiries(id, pageable);
    }

    @Operation(summary = "상품 문의 등록")
    @PostMapping("/product/{id}")
    public void createProductInquiry(@PathVariable Long id, @RequestPart CreateInquiryRequest request, @RequestPart List<MultipartFile> images) {
        inquiryService.createInquiry(id, request, images);
    }

    @Operation(summary = "사용자(본인) 문의 목록 조회")
    @GetMapping("/user")
    public PageResponse<GetUserInquiryResponse> getUserInquiries(Pageable pageable) {
        return inquiryService.getProductInquiriesByUser(pageable);
    }
}
