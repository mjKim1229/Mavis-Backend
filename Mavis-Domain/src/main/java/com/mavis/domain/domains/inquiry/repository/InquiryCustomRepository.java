package com.mavis.domain.domains.inquiry.repository;

import com.mavis.domain.domains.inquiry.domain.Inquiry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InquiryCustomRepository {
    Page<Inquiry> findInquiryByProductId(Long productId, Pageable pageable);
}
