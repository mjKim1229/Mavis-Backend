package com.mavis.api.inquiry.implement;

import com.mavis.api.common.page.PageResponse;
import com.mavis.api.inquiry.dto.GetProductInquiryResponse;
import com.mavis.api.inquiry.dto.InquiryAnswerResponse;
import com.mavis.api.inquiry.dto.InquiryResponse;
import com.mavis.domain.domains.inquiry.domain.AnswerStatus;
import com.mavis.domain.domains.inquiry.domain.Inquiry;
import com.mavis.domain.domains.inquiry.domain.InquiryAnswer;
import com.mavis.domain.domains.inquiry.exception.InquiryNotFoundException;
import com.mavis.domain.domains.inquiry.repository.InquiryAnswerRepository;
import com.mavis.domain.domains.inquiry.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    public PageResponse<GetProductInquiryResponse> readProductInquiries(Long productId, boolean onlyUnanswered, Pageable pageable) {
        Page<Inquiry> inquiries = inquiryRepository.findInquiryByProductId(productId, onlyUnanswered, pageable);
        Page<GetProductInquiryResponse> inquiryPages = inquiries.map(this::toProductInquiryResponse);
        return PageResponse.of(inquiryPages);
    }

    private GetProductInquiryResponse toProductInquiryResponse(Inquiry inquiry) {
        InquiryAnswer answer = inquiryAnswerRepository.findByInquiryAndIsDeletedFalse(inquiry).orElse(null);
        InquiryResponse inquiryResponse = inquiry.isPrivate() ? null : InquiryResponse.from(inquiry, inquiry.getUser());
        InquiryAnswerResponse inquiryAnswerResponse = resolveAnswerResponse(inquiry, answer);
        AnswerStatus answerStatus = resolveAnswerStatus(answer);

        return GetProductInquiryResponse.builder()
                .inquiry(inquiryResponse)
                .inquiryAnswer(inquiryAnswerResponse)
                .isPrivate(inquiry.isPrivate())
                .answerStatus(answerStatus)
                .build();
    }

    private InquiryAnswerResponse resolveAnswerResponse(Inquiry inquiry, InquiryAnswer answer) {
        if (inquiry.isPrivate() || answer == null) return null;
        return InquiryAnswerResponse.from(answer);
    }

    private AnswerStatus resolveAnswerStatus(InquiryAnswer answer) {
        if (answer != null) return AnswerStatus.ANSWERED;
        return AnswerStatus.UNANSWERED;
    }
}
