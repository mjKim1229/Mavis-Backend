package com.mavis.admin.domains.inquiry.service;

import com.mavis.domain.domains.inquiry.domain.Inquiry;
import com.mavis.domain.domains.inquiry.domain.InquiryAnswer;
import com.mavis.domain.domains.inquiry.implement.InquiryReader;
import com.mavis.domain.domains.inquiry.repository.InquiryAnswerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InquiryAnswerService {
    private final InquiryAnswerRepository inquiryAnswerRepository;
    private final InquiryReader inquiryReader;

    @Transactional
    public void createInquiryAnswer(Long inquiryId, String answer) {
        Inquiry inquiry = inquiryReader.findById(inquiryId);
        InquiryAnswer inquiryAnswer = InquiryAnswer.builder()
                .answer(answer)
                .inquiry(inquiry)
                .build();
        inquiryAnswerRepository.save(inquiryAnswer);
    }
}
