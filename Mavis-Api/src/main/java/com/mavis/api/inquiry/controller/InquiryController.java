package com.mavis.api.inquiry.controller;

import com.mavis.api.inquiry.dto.CreateInquiryRequest;
import com.mavis.api.inquiry.dto.GetProductInquiryResponse;
import com.mavis.api.inquiry.service.InquiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/api/inquiry")
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;

    @GetMapping("/product/{id}")
    public List<GetProductInquiryResponse> getProductInquiries(@PathVariable Long id) {
        return inquiryService.getProductInquiries(id);
    }

    @PostMapping("/product/{id}")
    public void createProductInquiry(@RequestBody CreateInquiryRequest request) {
        inquiryService.createInquiry(request);
    }
}
