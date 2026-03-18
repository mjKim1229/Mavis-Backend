package com.mavis.domain.domains.inquiry.exception;

import com.mavis.common.exception.MavisCodeException;

public class InquiryAnswerNotFoundException extends MavisCodeException {

    public static final MavisCodeException EXCEPTION = new InquiryAnswerNotFoundException();

    private InquiryAnswerNotFoundException() {
        super(InquiryErrorCode.INQUIRY_ANSWER_NOT_FOUND);
    }
}
