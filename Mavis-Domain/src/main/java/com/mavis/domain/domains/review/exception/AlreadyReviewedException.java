package com.mavis.domain.domains.review.exception;

import com.mavis.common.exception.MavisCodeException;

public class AlreadyReviewedException extends MavisCodeException {
    public static final MavisCodeException Exception = new AlreadyReviewedException();

    private AlreadyReviewedException() {
        super(ReviewErrorCode.ALREADY_REVIEWED);
    }
}
