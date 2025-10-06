package com.mavis.domain.domains.review.exception;

import com.mavis.common.exception.MavisCodeException;

public class ReviewOrderUserNotMatchException extends MavisCodeException {
    public static final MavisCodeException Exception = new ReviewOrderUserNotMatchException();

    private ReviewOrderUserNotMatchException() {
        super(ReviewErrorCode.REVIEW_ORDER_USER_NOT_MATCH);
    }
}
