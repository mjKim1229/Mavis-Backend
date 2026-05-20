package com.mavis.api.review.dto;

import java.util.List;

public record UpdateReviewRequest(
        String content,
        List<ReviewImageVO> keepImages
) {
}
