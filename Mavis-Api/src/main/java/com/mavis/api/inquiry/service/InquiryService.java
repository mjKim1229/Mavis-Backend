package com.mavis.api.inquiry.service;

import com.mavis.api.auth.implement.UserReader;
import com.mavis.api.inquiry.dto.CreateInquiryRequest;
import com.mavis.api.inquiry.dto.GetProductInquiryResponse;
import com.mavis.api.inquiry.implement.InquiryReader;
import com.mavis.domains.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InquiryService {

    private final InquiryReader inquiryReader;
    private final UserReader userReader;

    @Transactional(readOnly = true)
    public List<GetProductInquiryResponse> getProductInquiries(Long productId) {
        return inquiryReader.readProductInquiries(productId);
    }

    @Transactional
    public void createInquiry(CreateInquiryRequest request) {
        User user = userReader.getCurrentUser();
        
    }
}
