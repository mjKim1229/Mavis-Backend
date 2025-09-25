package com.mavis.domain.domains.inquiry.exception;

import com.mavis.common.exception.MavisCodeException;

public class InquiryNotFoundException extends MavisCodeException {
    public static final MavisCodeException EXCEPTION = new InquiryNotFoundException();

    public InquiryNotFoundException() {
        super(InquiryErrorCode.INQUIRY_NOT_FOUND);
    }
}
