package com.mavis.domain.domains.review.repository;

import com.mavis.domain.domains.review.domain.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {
}
