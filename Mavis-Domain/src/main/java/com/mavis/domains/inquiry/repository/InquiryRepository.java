package com.mavis.domains.inquiry.repository;

import com.mavis.domains.inquiry.domain.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
    List<Inquiry> findAllByProductIdAndIsDeletedFalse(Long ProductId);
}
