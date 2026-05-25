package com.mavis.domain.domains.inquiry.repository;

import com.mavis.domain.domains.inquiry.domain.Inquiry;
import com.mavis.domain.domains.inquiry.domain.InquiryAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InquiryAnswerRepository extends JpaRepository<InquiryAnswer, Long> {
    boolean existsByInquiryAndIsDeletedFalse(Inquiry inquiry);
    Optional<InquiryAnswer> findByInquiryAndIsDeletedFalse(Inquiry inquiry);
}
