package com.mavis.api.inquiry.service;

import com.mavis.api.inquiry.dto.InquiryAnswerResponse;
import com.mavis.domain.domains.inquiry.domain.AnswerStatus;
import com.mavis.domain.domains.inquiry.domain.Inquiry;
import com.mavis.domain.domains.inquiry.domain.InquiryAnswer;
import com.mavis.domain.domains.inquiry.exception.InquiryAlreadyAnsweredCannotDeleteException;
import com.mavis.domain.domains.inquiry.repository.InquiryAnswerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InquiryAnswerReader {

    private final InquiryAnswerRepository inquiryAnswerRepository;

    public InquiryAnswer findByInquiry(Inquiry inquiry) {
        return inquiryAnswerRepository.findByInquiryAndIsDeletedFalse(inquiry).orElse(null);
    }

    public void validateNotAnswered(Inquiry inquiry) {
        if (inquiryAnswerRepository.existsByInquiryAndIsDeletedFalse(inquiry)) {
            throw InquiryAlreadyAnsweredCannotDeleteException.EXCEPTION;
        }
    }

    public InquiryAnswerResult resolveForInquiry(Inquiry inquiry) {
        InquiryAnswer answer = findByInquiry(inquiry);
        InquiryAnswerResponse answerResponse = resolveAnswerResponse(inquiry, answer);
        AnswerStatus answerStatus = resolveAnswerStatus(answer);
        return new InquiryAnswerResult(answerResponse, answerStatus);
    }

    private AnswerStatus resolveAnswerStatus(InquiryAnswer answer) {
        return answer != null ? AnswerStatus.ANSWERED : AnswerStatus.UNANSWERED;
    }

    private InquiryAnswerResponse resolveAnswerResponse(Inquiry inquiry, InquiryAnswer answer) {
        if (inquiry.isPrivate() || answer == null) return null;
        return InquiryAnswerResponse.from(answer);
    }

    public UserInquiryAnswerResult resolveForUserInquiry(Inquiry inquiry) {
        InquiryAnswer answer = findByInquiry(inquiry);
        if (answer == null) {
            return new UserInquiryAnswerResult(null, null);
        }
        return new UserInquiryAnswerResult(answer.getAnswer(), answer.getCreatedAt());
    }
}
