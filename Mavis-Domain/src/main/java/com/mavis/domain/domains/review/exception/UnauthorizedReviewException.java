package com.mavis.domain.domains.review.exception;

import com.mavis.common.exception.MavisCodeException;

public class UnauthorizedReviewException extends MavisCodeException {
    public static final MavisCodeException EXCEPTION = new UnauthorizedReviewException();

    private UnauthorizedReviewException() {
        super(ReviewErrorCode.UNAUTHORIZED_REVIEW);
    }
}
