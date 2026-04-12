package com.mavis.domain.domains.inquiry.repository;

import com.mavis.domain.domains.inquiry.domain.AnswerStatus;
import com.mavis.domain.domains.inquiry.domain.Inquiry;
import com.mavis.domain.domains.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InquiryCustomRepository {
    Page<Inquiry> findInquiryByProductId(Long productId, Pageable pageable);
    Page<Inquiry> findInquiryByUser(User user, Pageable pageable);
    Page<Inquiry> findAllInquiries(AnswerStatus status, Pageable pageable);
}
