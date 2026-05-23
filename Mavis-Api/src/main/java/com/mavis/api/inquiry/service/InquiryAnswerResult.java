package com.mavis.api.inquiry.service;

import com.mavis.api.inquiry.dto.InquiryAnswerResponse;
import com.mavis.domain.domains.inquiry.domain.AnswerStatus;

public record InquiryAnswerResult(
        InquiryAnswerResponse answerResponse,
        AnswerStatus answerStatus
) {
}
