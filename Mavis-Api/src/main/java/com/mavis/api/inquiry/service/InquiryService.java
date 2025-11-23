package com.mavis.api.inquiry.service;

import com.mavis.api.auth.implement.UserReader;
import com.mavis.api.common.page.PageResponse;
import com.mavis.api.inquiry.dto.CreateInquiryRequest;
import com.mavis.api.inquiry.dto.GetProductInquiryResponse;
import com.mavis.api.inquiry.dto.GetUserInquiryResponse;
import com.mavis.api.inquiry.implement.InquiryImageAppender;
import com.mavis.api.inquiry.implement.InquiryReader;
import com.mavis.domain.domains.inquiry.domain.Inquiry;
import com.mavis.domain.domains.inquiry.domain.InquiryAnswer;
import com.mavis.domain.domains.inquiry.repository.InquiryRepository;
import com.mavis.domain.domains.product.domain.Product;
import com.mavis.domain.domains.product.implement.ProductReader;
import com.mavis.domain.domains.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InquiryService {

    private final InquiryReader inquiryReader;
    private final UserReader userReader;
    private final ProductReader productReader;
    private final InquiryRepository inquiryRepository;
    private final InquiryImageAppender inquiryImageAppender;

    @Transactional(readOnly = true)
    public PageResponse<GetProductInquiryResponse> getProductInquiries(Long productId, Pageable pageable) {
        Product product = productReader.readById(productId);
        return inquiryReader.readProductInquiries(product.getId(), pageable);
    }

    @Transactional
    public void createInquiry(Long productId, CreateInquiryRequest request, List<MultipartFile> images) {
        User user = userReader.getCurrentUser();
        Product product = productReader.readById(productId);
        Inquiry inquiry = request.toEntity(product, user);
        Inquiry savedInquiry = inquiryRepository.save(inquiry);
        inquiryImageAppender.saveInquiryImages(savedInquiry, images);
    }

    @Transactional(readOnly = true)
    public PageResponse<GetUserInquiryResponse> getProductInquiriesByUser(Pageable pageable) {
        User user = userReader.getCurrentUser();
        Page<Inquiry> userInquiryPages = inquiryRepository.findInquiryByUser(user, pageable);
        Page<GetUserInquiryResponse> getUserInquiryResponsePage = userInquiryPages.map(
                inquiry -> {
                    if (inquiry.getInquiryAnswer() == null) {
                        return new GetUserInquiryResponse(inquiry.getQuestion(), inquiry.getCreatedAt(), null, null);
                    }
                    InquiryAnswer inquiryAnswer = inquiry.getInquiryAnswer();
                    return new GetUserInquiryResponse(inquiry.getQuestion(), inquiry.getCreatedAt(), inquiryAnswer.getAnswer(), inquiryAnswer.getCreatedAt());
                }
        );
        return PageResponse.of(getUserInquiryResponsePage);
    }
}
