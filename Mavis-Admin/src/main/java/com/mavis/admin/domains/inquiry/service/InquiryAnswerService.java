package com.mavis.admin.domains.inquiry.service;

import com.mavis.admin.domains.admin.implement.AdminReader;
import com.mavis.domain.domains.admin.domain.Admin;
import com.mavis.domain.domains.inquiry.domain.Inquiry;
import com.mavis.domain.domains.inquiry.domain.InquiryAnswer;
import com.mavis.domain.domains.inquiry.exception.InquiryAlreadyAnsweredException;
import com.mavis.domain.domains.inquiry.implement.InquiryDomainReader;
import com.mavis.domain.domains.inquiry.repository.InquiryAnswerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InquiryAnswerService {
    private final InquiryAnswerRepository inquiryAnswerRepository;
    private final InquiryDomainReader inquiryDomainReader;
    private final AdminReader adminReader;

    @Transactional
    public void createInquiryAnswer(Long inquiryId, String answer) {
        Inquiry inquiry = inquiryDomainReader.findById(inquiryId);
        if (inquiryAnswerRepository.existsByInquiryAndIsDeletedFalse(inquiry)) {
            throw InquiryAlreadyAnsweredException.EXCEPTION;
        }
        Admin admin = adminReader.getCurrentAdmin();
        InquiryAnswer inquiryAnswer = InquiryAnswer.builder()
                .answer(answer)
                .inquiry(inquiry)
                .admin(admin)
                .build();
        inquiryAnswerRepository.save(inquiryAnswer);
    }
}
}
