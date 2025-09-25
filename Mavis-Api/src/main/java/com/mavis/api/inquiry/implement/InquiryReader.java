package com.mavis.api.inquiry.implement;

import com.mavis.api.common.page.PageResponse;
import com.mavis.api.inquiry.dto.GetProductInquiryResponse;
import com.mavis.api.inquiry.dto.InquiryAnswerResponse;
import com.mavis.api.inquiry.dto.InquiryResponse;
import com.mavis.domain.domains.inquiry.domain.Inquiry;
import com.mavis.domain.domains.inquiry.exception.InquiryNotFoundException;
import com.mavis.domain.domains.inquiry.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InquiryReader {

    private final InquiryRepository inquiryRepository;

    public Inquiry findById(Long inquiryId) {
        return inquiryRepository.findByIdAndIsDeletedFalse(inquiryId)
                .orElseThrow(() -> InquiryNotFoundException.EXCEPTION);
    }

    public PageResponse<GetProductInquiryResponse> readProductInquiries(Long productId, Pageable pageable) {
        Page<Inquiry> inquiries = inquiryRepository.findInquiryByProductId(productId, pageable);
        Page<GetProductInquiryResponse> inquiryPages = inquiries.map(this::toProductInquiryResponse);
        return PageResponse.of(inquiryPages);
    }

    private GetProductInquiryResponse toProductInquiryResponse(Inquiry inquiry) {
        InquiryResponse inquiryResponse = toInquiryResponse(inquiry);
        InquiryAnswerResponse inquiryAnswerResponse = toInquiryAnswerResponse(inquiry);

        return GetProductInquiryResponse.builder()
                .inquiry(inquiryResponse)
                .inquiryAnswer(inquiryAnswerResponse)
                .isPrivate(inquiry.isPrivate())
                .build();
    }

    private InquiryResponse toInquiryResponse(Inquiry inquiry) {
        if (inquiry.isPrivate()) {
            return null;
        }
        return InquiryResponse.from(inquiry, inquiry.getUser());
    }

    private InquiryAnswerResponse toInquiryAnswerResponse(Inquiry inquiry) {
        if (inquiry.isPrivate() || inquiry.getInquiryAnswer() == null) {
            return null;
        }
        return InquiryAnswerResponse.from(inquiry.getInquiryAnswer());
    }
}
