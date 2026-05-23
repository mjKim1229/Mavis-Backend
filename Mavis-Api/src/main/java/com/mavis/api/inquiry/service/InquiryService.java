package com.mavis.api.inquiry.service;

import com.mavis.api.auth.implement.UserReader;
import com.mavis.api.common.page.PageResponse;
import com.mavis.api.inquiry.dto.CreateInquiryRequest;
import com.mavis.api.inquiry.dto.GetProductInquiryResponse;
import com.mavis.api.inquiry.dto.GetUserInquiryResponse;
import com.mavis.api.inquiry.implement.InquiryReader;
import com.mavis.common.util.DateFormatters;
import com.mavis.domain.domains.inquiry.domain.Inquiry;
import com.mavis.domain.domains.inquiry.domain.InquiryAnswer;
import com.mavis.domain.domains.inquiry.exception.InquiryAlreadyAnsweredCannotDeleteException;
import com.mavis.domain.domains.inquiry.exception.UnauthorizedInquiryException;
import com.mavis.domain.domains.inquiry.repository.InquiryAnswerRepository;
import com.mavis.domain.domains.inquiry.repository.InquiryRepository;
import com.mavis.domain.domains.product.domain.Product;
import com.mavis.domain.domains.product.implement.ProductReader;
import com.mavis.domain.domains.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@RequiredArgsConstructor
public class InquiryService {

    private final InquiryReader inquiryReader;
    private final UserReader userReader;
    private final ProductReader productReader;
    private final InquiryRepository inquiryRepository;
    private final InquiryAnswerRepository inquiryAnswerRepository;

    @Transactional(readOnly = true)
    public PageResponse<GetProductInquiryResponse> getProductInquiries(Long productId, boolean onlyUnanswered, Pageable pageable) {
        Product product = productReader.readById(productId);
        return inquiryReader.readProductInquiries(product.getId(), onlyUnanswered, pageable);
    }

    @Transactional
    public void deleteInquiry(Long inquiryId) {
        User user = userReader.getCurrentUser();
        Inquiry inquiry = inquiryReader.findById(inquiryId);
        if (!inquiry.getUser().getId().equals(user.getId())) {
            throw UnauthorizedInquiryException.EXCEPTION;
        }
        if (inquiryAnswerRepository.existsByInquiryAndIsDeletedFalse(inquiry)) {
            throw InquiryAlreadyAnsweredCannotDeleteException.EXCEPTION;
        }
        inquiry.delete();
    }

    @Transactional
    public void createInquiry(Long productId, CreateInquiryRequest request) {
        User user = userReader.getCurrentUser();
        Product product = productReader.readById(productId);
        Inquiry inquiry = request.toEntity(product, user);
        inquiryRepository.save(inquiry);
    }

    @Transactional(readOnly = true)
    public PageResponse<GetUserInquiryResponse> getProductInquiriesByUser(Pageable pageable) {
        User user = userReader.getCurrentUser();
        Page<Inquiry> userInquiryPages = inquiryRepository.findInquiryByUser(user, pageable);
        Page<GetUserInquiryResponse> getUserInquiryResponsePage = userInquiryPages.map(
                inquiry -> {
                    String questionCreatedAt = inquiry.getCreatedAt().format(DateFormatters.DATE_FORMATTER);
                    InquiryAnswer inquiryAnswer = inquiryAnswerRepository.findByInquiryAndIsDeletedFalse(inquiry).orElse(null);
                    if (inquiryAnswer == null) {
                        return new GetUserInquiryResponse(inquiry.getId(), inquiry.getProduct().getId(), inquiry.getProduct().getName(), inquiry.getQuestion(), questionCreatedAt, null, null);
                    }
                    String answerCreatedAt = inquiryAnswer.getCreatedAt().format(DateFormatters.DATE_FORMATTER);
                    return new GetUserInquiryResponse(inquiry.getId(), inquiry.getProduct().getId(), inquiry.getProduct().getName(), inquiry.getQuestion(), questionCreatedAt, inquiryAnswer.getAnswer(), answerCreatedAt);
                }
        );
        return PageResponse.of(getUserInquiryResponsePage);
    }
}
