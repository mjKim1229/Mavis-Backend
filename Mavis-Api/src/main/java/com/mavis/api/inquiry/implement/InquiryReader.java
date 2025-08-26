package com.mavis.api.inquiry.implement;

import com.mavis.api.inquiry.domain.Inquiry;
import com.mavis.api.inquiry.dto.GetProductInquiryResponse;
import com.mavis.api.inquiry.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class InquiryReader {

    private final InquiryRepository inquiryRepository;

    public List<GetProductInquiryResponse> readProductInquiries(Long productId) {
        List<Inquiry> inquiries = inquiryRepository.findAllByProductIdAndIsDeletedFalse(productId);
        return inquiries.stream()
                .map(this::processPrivate)
                .toList();
    }

    private GetProductInquiryResponse processPrivate(Inquiry inquiry) {
        if (inquiry.isPrivate()) {
            return GetProductInquiryResponse.builder()
                    .isPrivate(true)
                    .build();
        }
        return GetProductInquiryResponse.from(inquiry);
    }
}
