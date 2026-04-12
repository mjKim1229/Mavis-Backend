package com.mavis.domain.domains.review.repository;

import com.mavis.domain.domains.review.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long>, ReviewCustomRepository {
    Optional<Review> findByIdAndIsDeletedFalse(Long id);
}
