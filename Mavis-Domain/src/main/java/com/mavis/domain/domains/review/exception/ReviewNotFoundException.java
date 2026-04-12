package com.mavis.domain.domains.review.exception;

import com.mavis.common.exception.MavisCodeException;

public class ReviewNotFoundException extends MavisCodeException {
    public static final MavisCodeException EXCEPTION = new ReviewNotFoundException();

    private ReviewNotFoundException() {
        super(ReviewErrorCode.REVIEW_NOT_FOUND);
    }
}
