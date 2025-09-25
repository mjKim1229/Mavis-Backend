package com.mavis.domain.domains.inquiry.implement;

import com.mavis.domain.domains.inquiry.domain.Inquiry;
import com.mavis.domain.domains.inquiry.exception.InquiryNotFoundException;
import com.mavis.domain.domains.inquiry.repository.InquiryAnswerRepository;
import com.mavis.domain.domains.inquiry.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InquiryReader {

    private final InquiryRepository inquiryRepository;
    private final InquiryAnswerRepository inquiryAnswerRepository;

    public Inquiry findById(Long inquiryId) {
        return inquiryRepository.findByIdAndIsDeletedFalse(inquiryId)
                .orElseThrow(() -> InquiryNotFoundException.EXCEPTION);
    }
}
