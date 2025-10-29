package com.mavis.domain.domains.inquiry.exception;

import com.mavis.common.exception.MavisCodeException;

public class InquiryAlreadyAnsweredException extends MavisCodeException {

    public static final MavisCodeException EXCEPTION = new InquiryAlreadyAnsweredException();

    private InquiryAlreadyAnsweredException() {
        super(InquiryErrorCode.INQUIRY_ALREADY_ANSWERED);
    }
}
