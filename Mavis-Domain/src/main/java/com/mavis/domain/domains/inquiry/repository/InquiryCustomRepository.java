package com.mavis.domain.domains.inquiry.repository;

import com.mavis.domain.domains.inquiry.domain.Inquiry;

import java.util.List;

public interface InquiryCustomRepository {
    List<Inquiry> findInquiryByProductId(Long productId);
}
