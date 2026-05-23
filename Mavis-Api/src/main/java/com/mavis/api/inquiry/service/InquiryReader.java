package com.mavis.api.inquiry.service;

import com.mavis.api.inquiry.dto.InquiryResponse;
import com.mavis.domain.domains.inquiry.domain.Inquiry;
import com.mavis.domain.domains.inquiry.exception.InquiryNotFoundException;
import com.mavis.domain.domains.inquiry.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InquiryReader {

    private final InquiryRepository inquiryRepository;

    public Inquiry findById(Long inquiryId) {
        return inquiryRepository.findByIdAndIsDeletedFalse(inquiryId)
                .orElseThrow(() -> InquiryNotFoundException.EXCEPTION);
    }

    public InquiryResponse resolveInquiryResponse(Inquiry inquiry) {
        if (inquiry.isPrivate()) {
            return null;
        }
        return InquiryResponse.from(inquiry, inquiry.getUser());
    }

    public UserInquiryResult resolveUserInquiry(Inquiry inquiry) {
        return new UserInquiryResult(
                inquiry.getId(),
                inquiry.getProduct().getId(),
                inquiry.getProduct().getName(),
                inquiry.getQuestion(),
                inquiry.getCreatedAt()
        );
    }
}
