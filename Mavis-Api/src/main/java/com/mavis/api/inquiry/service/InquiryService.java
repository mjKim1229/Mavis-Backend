package com.mavis.api.inquiry.service;

import com.mavis.api.auth.implement.UserReader;
import com.mavis.api.common.page.PageResponse;
import com.mavis.api.inquiry.dto.*;
import com.mavis.domain.domains.inquiry.domain.AnswerStatus;
import com.mavis.domain.domains.inquiry.domain.Inquiry;
import com.mavis.domain.domains.inquiry.dto.ProductInquiryRow;
import com.mavis.domain.domains.inquiry.dto.UserInquiryRow;
import com.mavis.domain.domains.inquiry.exception.UnauthorizedInquiryException;
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
    private final InquiryAnswerReader inquiryAnswerReader;
    private final UserReader userReader;
    private final ProductReader productReader;
    private final InquiryRepository inquiryRepository;

    @Transactional(readOnly = true)
    public PageResponse<GetProductInquiryResponse> getProductInquiries(Long productId, boolean onlyUnanswered, Pageable pageable) {
        Product product = productReader.readById(productId);
        Page<ProductInquiryRow> dtoPage = inquiryRepository.findInquiryByProduct(product, onlyUnanswered, pageable);
        Page<GetProductInquiryResponse> getProductInquiryResponsePage = dtoPage.map(dto -> {
            InquiryResponse inquiryResponse = dto.isPrivate() ? null : InquiryResponse.from(dto);
            InquiryAnswerResponse answerResponse = (dto.isPrivate() || dto.answer() == null) ? null : InquiryAnswerResponse.from(dto);
            AnswerStatus answerStatus = dto.answer() != null ? AnswerStatus.ANSWERED : AnswerStatus.UNANSWERED;
            return GetProductInquiryResponse.builder()
                    .inquiry(inquiryResponse)
                    .inquiryAnswer(answerResponse)
                    .isPrivate(dto.isPrivate())
                    .answerStatus(answerStatus)
                    .build();
        });
        return PageResponse.of(getProductInquiryResponsePage);
    }

    @Transactional
    public void deleteInquiry(Long inquiryId) {
        User user = userReader.getCurrentUser();
        Inquiry inquiry = inquiryReader.findById(inquiryId);
        if (!inquiry.getUser().getId().equals(user.getId())) {
            throw UnauthorizedInquiryException.EXCEPTION;
        }
        inquiryAnswerReader.validateNotAnswered(inquiry);
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
        Page<UserInquiryRow> dtoPage = inquiryRepository.findInquiryByUser(user, pageable);
        Page<GetUserInquiryResponse> getUserInquiryResponsePage = dtoPage.map(GetUserInquiryResponse::from);
        return PageResponse.of(getUserInquiryResponsePage);
    }
}
