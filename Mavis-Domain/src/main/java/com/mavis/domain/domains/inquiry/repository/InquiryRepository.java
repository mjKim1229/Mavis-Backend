package com.mavis.domain.domains.inquiry.repository;

import com.mavis.domain.domains.inquiry.domain.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
    List<Inquiry> findAllByProductIdAndIsDeletedFalse(Long ProductId);

    Optional<Inquiry> findByIdAndIsDeletedFalse(Long id);
}
