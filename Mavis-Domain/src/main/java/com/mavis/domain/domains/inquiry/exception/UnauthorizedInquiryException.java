package com.mavis.domain.domains.inquiry.exception;

import com.mavis.common.exception.MavisCodeException;

public class UnauthorizedInquiryException extends MavisCodeException {
    public static final MavisCodeException EXCEPTION = new UnauthorizedInquiryException();

    private UnauthorizedInquiryException() {
        super(InquiryErrorCode.UNAUTHORIZED_INQUIRY);
    }
}
