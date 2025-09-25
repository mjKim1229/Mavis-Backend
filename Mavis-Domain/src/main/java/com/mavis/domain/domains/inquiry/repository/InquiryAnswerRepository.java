package com.mavis.domain.domains.inquiry.repository;

import com.mavis.domain.domains.inquiry.domain.InquiryAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryAnswerRepository extends JpaRepository<InquiryAnswer, Long> {
}
