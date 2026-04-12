package com.mavis.api.inquiry.implement;

import com.mavis.api.common.page.PageResponse;
import com.mavis.api.inquiry.dto.GetProductInquiryResponse;
import com.mavis.api.inquiry.dto.InquiryAnswerResponse;
import com.mavis.api.inquiry.dto.InquiryResponse;
import com.mavis.domain.domains.inquiry.domain.AnswerStatus;
import com.mavis.domain.domains.inquiry.domain.Inquiry;
import com.mavis.domain.domains.inquiry.domain.InquiryAnswer;
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

    public PageResponse<GetProductInquiryResponse> readProductInquiries(Long productId, boolean onlyUnanswered, Pageable pageable) {
        Page<Inquiry> inquiries = inquiryRepository.findInquiryByProductId(productId, onlyUnanswered, pageable);
        Page<GetProductInquiryResponse> inquiryPages = inquiries.map(this::toProductInquiryResponse);
        return PageResponse.of(inquiryPages);
    }

    private GetProductInquiryResponse toProductInquiryResponse(Inquiry inquiry) {
        InquiryResponse inquiryResponse = inquiry.isPrivate() ? null : InquiryResponse.from(inquiry, inquiry.getUser());
        InquiryAnswerResponse inquiryAnswerResponse = resolveAnswerResponse(inquiry);
        AnswerStatus answerStatus = resolveAnswerStatus(inquiry);

        return GetProductInquiryResponse.builder()
                .inquiry(inquiryResponse)
                .inquiryAnswer(inquiryAnswerResponse)
                .isPrivate(inquiry.isPrivate())
                .answerStatus(answerStatus)
                .build();
    }

    private InquiryAnswerResponse resolveAnswerResponse(Inquiry inquiry) {
        if (inquiry.isPrivate()) return null;
        InquiryAnswer answer = inquiry.getInquiryAnswer();
        if (answer == null || answer.isDeleted()) return null;
        return InquiryAnswerResponse.from(answer);
    }

    private AnswerStatus resolveAnswerStatus(Inquiry inquiry) {
        InquiryAnswer answer = inquiry.getInquiryAnswer();
        if (answer != null && !answer.isDeleted()) return AnswerStatus.ANSWERED;
        return AnswerStatus.UNANSWERED;
    }
}
