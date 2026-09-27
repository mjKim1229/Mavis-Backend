package com.mavis.domain.domains.review.exception;

import com.mavis.common.exception.MavisCodeException;

public class ReviewNotWritableException extends MavisCodeException {
    public static final MavisCodeException Exception = new ReviewNotWritableException();

    private ReviewNotWritableException() {
        super(ReviewErrorCode.REVIEW_NOT_WRITABLE);
    }
}
