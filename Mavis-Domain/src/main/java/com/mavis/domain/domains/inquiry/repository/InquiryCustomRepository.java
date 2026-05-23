package com.mavis.domain.domains.inquiry.repository;

import com.mavis.domain.domains.inquiry.domain.AnswerStatus;
import com.mavis.domain.domains.inquiry.domain.Inquiry;
import com.mavis.domain.domains.inquiry.dto.ProductInquiryRow;
import com.mavis.domain.domains.inquiry.dto.UserInquiryRow;
import com.mavis.domain.domains.product.domain.Product;
import com.mavis.domain.domains.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InquiryCustomRepository {
    Page<ProductInquiryRow> findInquiryByProduct(Product product, boolean onlyUnanswered, Pageable pageable);
    Page<UserInquiryRow> findInquiryByUser(User user, Pageable pageable);
    Page<Inquiry> findAllInquiries(AnswerStatus status, Pageable pageable);
}
