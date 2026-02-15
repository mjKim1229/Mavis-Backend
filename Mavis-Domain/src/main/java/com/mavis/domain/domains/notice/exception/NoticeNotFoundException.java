package com.mavis.domain.domains.notice.exception;

import com.mavis.common.exception.MavisCodeException;

public class NoticeNotFoundException extends MavisCodeException {
    public static final MavisCodeException EXCEPTION = new NoticeNotFoundException();

    private NoticeNotFoundException() {
        super(NoticeErrorCode.NOTICE_NOT_FOUND);
    }
}
