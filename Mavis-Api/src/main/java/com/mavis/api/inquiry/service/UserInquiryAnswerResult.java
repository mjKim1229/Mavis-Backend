package com.mavis.api.inquiry.service;

import java.time.LocalDateTime;

public record UserInquiryAnswerResult(
        String answer,
        LocalDateTime answerCreatedAt
) {
}
