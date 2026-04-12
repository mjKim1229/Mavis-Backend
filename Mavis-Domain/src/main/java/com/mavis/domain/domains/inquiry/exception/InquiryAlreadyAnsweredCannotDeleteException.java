package com.mavis.domain.domains.inquiry.exception;

import com.mavis.common.exception.MavisCodeException;

public class InquiryAlreadyAnsweredCannotDeleteException extends MavisCodeException {
    public static final MavisCodeException EXCEPTION = new InquiryAlreadyAnsweredCannotDeleteException();

    private InquiryAlreadyAnsweredCannotDeleteException() {
        super(InquiryErrorCode.INQUIRY_ALREADY_ANSWERED_CANNOT_DELETE);
    }
}
