package com.mavis.api.inquiry.service;

import com.mavis.domain.domains.inquiry.domain.Inquiry;
import com.mavis.domain.domains.inquiry.exception.InquiryAlreadyAnsweredCannotDeleteException;
import com.mavis.domain.domains.inquiry.repository.InquiryAnswerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InquiryAnswerReader {

    private final InquiryAnswerRepository inquiryAnswerRepository;

    public void validateNotAnswered(Inquiry inquiry) {
        if (inquiryAnswerRepository.existsByInquiryAndIsDeletedFalse(inquiry)) {
            throw InquiryAlreadyAnsweredCannotDeleteException.EXCEPTION;
        }
    }
}
