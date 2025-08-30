package com.mavis.api.review.dto;

public record ProductReviewTotal(
        Double averageScore,
        Long count
) {
}
