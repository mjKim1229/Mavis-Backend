package com.mavis.admin.domains.inquiry.service;

import com.mavis.admin.domains.inquiry.dto.GetAdminInquiryDetailResponse;
import com.mavis.admin.domains.inquiry.dto.GetAdminInquiryResponse;
import com.mavis.admin.common.page.PageResponse;
import com.mavis.domain.domains.inquiry.domain.AnswerStatus;
import com.mavis.domain.domains.inquiry.domain.Inquiry;
import com.mavis.domain.domains.inquiry.domain.InquiryAnswer;
import com.mavis.domain.domains.inquiry.implement.InquiryDomainReader;
import com.mavis.domain.domains.inquiry.repository.InquiryAnswerRepository;
import com.mavis.domain.domains.inquiry.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminInquiryService {

    private final InquiryRepository inquiryRepository;
    private final InquiryDomainReader inquiryDomainReader;
    private final InquiryAnswerRepository inquiryAnswerRepository;

    @Transactional(readOnly = true)
    public PageResponse<GetAdminInquiryResponse> getInquiries(AnswerStatus status, Pageable pageable) {
        return PageResponse.of(
                inquiryRepository.findAllInquiries(status, pageable)
                        .map(GetAdminInquiryResponse::from)
        );
    }

    @Transactional(readOnly = true)
    public GetAdminInquiryDetailResponse getInquiry(Long inquiryId) {
        Inquiry inquiry = inquiryDomainReader.findById(inquiryId);
        InquiryAnswer answer = inquiryAnswerRepository.findByInquiryAndIsDeletedFalse(inquiry).orElse(null);
        return GetAdminInquiryDetailResponse.from(inquiry, answer);
    }
}
