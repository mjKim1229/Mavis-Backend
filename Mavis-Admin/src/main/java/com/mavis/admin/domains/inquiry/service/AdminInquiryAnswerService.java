package com.mavis.admin.domains.inquiry.service;

import com.mavis.admin.domains.admin.implement.AdminReader;
import com.mavis.domain.domains.admin.domain.Admin;
import com.mavis.domain.domains.inquiry.domain.Inquiry;
import com.mavis.domain.domains.inquiry.domain.InquiryAnswer;
import com.mavis.domain.domains.inquiry.exception.InquiryAlreadyAnsweredException;
import com.mavis.domain.domains.inquiry.exception.InquiryAnswerNotFoundException;
import com.mavis.domain.domains.inquiry.exception.InquiryNotFoundException;
import com.mavis.domain.domains.inquiry.repository.InquiryAnswerRepository;
import com.mavis.domain.domains.inquiry.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminInquiryAnswerService {
    private final InquiryRepository inquiryRepository;
    private final InquiryAnswerRepository inquiryAnswerRepository;
    private final AdminReader adminReader;

    @Transactional
    public void createInquiryAnswer(Long inquiryId, String answer) {
        Inquiry inquiry = inquiryRepository.findByIdAndIsDeletedFalse(inquiryId)
                .orElseThrow(() -> InquiryNotFoundException.EXCEPTION);
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

    @Transactional
    public void updateInquiryAnswer(Long inquiryId, String answer) {
        Inquiry inquiry = inquiryRepository.findByIdAndIsDeletedFalse(inquiryId)
                .orElseThrow(() -> InquiryNotFoundException.EXCEPTION);
        InquiryAnswer inquiryAnswer = inquiryAnswerRepository.findByInquiryAndIsDeletedFalse(inquiry)
                .orElseThrow(() -> InquiryAnswerNotFoundException.EXCEPTION);
        inquiryAnswer.updateAnswer(answer);
    }

    @Transactional
    public void deleteInquiryAnswer(Long inquiryId) {
        Inquiry inquiry = inquiryRepository.findByIdAndIsDeletedFalse(inquiryId)
                .orElseThrow(() -> InquiryNotFoundException.EXCEPTION);
        InquiryAnswer inquiryAnswer = inquiryAnswerRepository.findByInquiryAndIsDeletedFalse(inquiry)
                .orElseThrow(() -> InquiryAnswerNotFoundException.EXCEPTION);
        inquiryAnswer.delete();
    }
}
