package com.mavis.api.inquiry.controller;

import com.mavis.api.inquiry.dto.CreateInquiryRequest;
import com.mavis.api.support.ControllerTestSupport;
import com.mavis.domain.domains.inquiry.domain.Inquiry;
import com.mavis.domain.domains.inquiry.domain.InquiryAnswer;
import com.mavis.domain.domains.inquiry.repository.InquiryAnswerRepository;
import com.mavis.domain.domains.inquiry.repository.InquiryRepository;
import com.mavis.domain.domains.product.domain.Product;
import com.mavis.domain.domains.product.repository.ProductRepository;
import com.mavis.domain.domains.user.domain.User;
import com.mavis.domain.domains.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class InquiryControllerTest extends ControllerTestSupport {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InquiryRepository inquiryRepository;

    @Autowired
    private InquiryAnswerRepository inquiryAnswerRepository;

    @Autowired
    private EntityManager em;

    private User savedUser;
    private Product savedProduct;

    @BeforeEach
    void setUp() {
        savedUser = userRepository.save(User.builder()
                .name("테스트유저")
                .email("test@test.com")
                .build());
        savedProduct = productRepository.save(Product.builder()
                .name("테스트상품")
                .price(10000)
                .build());
    }

    @Test
    void 상품_문의_목록_조회_성공() throws Exception {
        Inquiry savedInquiry = inquiryRepository.save(Inquiry.builder()
                .question("질문입니다")
                .isPrivate(false)
                .product(savedProduct)
                .user(savedUser)
                .build());

        mockMvc.perform(get("/v1/api/inquiry/product/{id}", savedProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].isPrivate").value(false))
                .andExpect(jsonPath("$.data.content[0].answerStatus").value("UNANSWERED"))
                .andExpect(jsonPath("$.data.content[0].inquiry.id").value(savedInquiry.getId()))
                .andExpect(jsonPath("$.data.content[0].inquiry.question").value("질문입니다"))
                .andExpect(jsonPath("$.data.content[0].inquiry.userName").value("테****"))
                .andExpect(jsonPath("$.data.content[0].inquiry.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.data.content[0].inquiryAnswer").isEmpty());
    }

    @Test
    void 비공개_문의는_내용_노출_안됨() throws Exception {
        inquiryRepository.save(Inquiry.builder()
                .question("비공개 질문입니다")
                .isPrivate(true)
                .product(savedProduct)
                .user(savedUser)
                .build());

        mockMvc.perform(get("/v1/api/inquiry/product/{id}", savedProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].isPrivate").value(true))
                .andExpect(jsonPath("$.data.content[0].answerStatus").value("UNANSWERED"))
                .andExpect(jsonPath("$.data.content[0].inquiry").isEmpty())
                .andExpect(jsonPath("$.data.content[0].inquiryAnswer").isEmpty());
    }

    @Test
    void 존재하지_않는_상품_문의_조회시_404() throws Exception {
        mockMvc.perform(get("/v1/api/inquiry/product/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void 미답변_문의만_조회_필터링() throws Exception {
        Inquiry unanswered = inquiryRepository.save(Inquiry.builder()
                .question("미답변 질문")
                .isPrivate(false)
                .product(savedProduct)
                .user(savedUser)
                .build());
        Inquiry answered = inquiryRepository.save(Inquiry.builder()
                .question("답변된 질문")
                .isPrivate(false)
                .product(savedProduct)
                .user(savedUser)
                .build());
        inquiryAnswerRepository.save(InquiryAnswer.builder()
                .answer("답변입니다")
                .inquiry(answered)
                .build());

        mockMvc.perform(get("/v1/api/inquiry/product/{id}", savedProduct.getId())
                        .param("onlyUnanswered", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].answerStatus").value("UNANSWERED"))
                .andExpect(jsonPath("$.data.content[0].inquiry.question").value("미답변 질문"))
                .andExpect(jsonPath("$.data.content[0].inquiry.userName").value("테****"))
                .andExpect(jsonPath("$.data.content[0].isPrivate").value(false))
                .andExpect(jsonPath("$.data.content[0].inquiryAnswer").isEmpty());
    }

    @Test
    void 상품_문의_등록_성공() throws Exception {
        mockMvc.perform(post("/v1/api/inquiry/product/{id}", savedProduct.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateInquiryRequest("질문내용입니다", false)))
                        .with(user(savedUser.getId().toString()).roles("USER")))
                .andExpect(status().isOk());
    }

    @Test
    void 비인증_문의_등록시_401() throws Exception {
        mockMvc.perform(post("/v1/api/inquiry/product/{id}", savedProduct.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateInquiryRequest("질문내용입니다", false))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 사용자_문의_목록_조회_성공() throws Exception {
        Inquiry savedInquiry = inquiryRepository.save(Inquiry.builder()
                .question("내 질문")
                .isPrivate(false)
                .product(savedProduct)
                .user(savedUser)
                .build());

        mockMvc.perform(get("/v1/api/inquiry/user")
                        .with(user(savedUser.getId().toString()).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].id").value(savedInquiry.getId()))
                .andExpect(jsonPath("$.data.content[0].productId").value(savedProduct.getId()))
                .andExpect(jsonPath("$.data.content[0].productName").value("테스트상품"))
                .andExpect(jsonPath("$.data.content[0].question").value("내 질문"))
                .andExpect(jsonPath("$.data.content[0].questionCreatedAt").isNotEmpty())
                .andExpect(jsonPath("$.data.content[0].answer").value((Object) null))
                .andExpect(jsonPath("$.data.content[0].answerCreatedAt").value((Object) null));
    }

    @Test
    void 사용자_문의_목록_조회_답변_포함() throws Exception {
        Inquiry inquiry = inquiryRepository.save(Inquiry.builder()
                .question("답변있는 질문")
                .isPrivate(false)
                .product(savedProduct)
                .user(savedUser)
                .build());
        inquiryAnswerRepository.save(InquiryAnswer.builder()
                .answer("관리자 답변")
                .inquiry(inquiry)
                .build());
        em.flush();
        em.clear();

        mockMvc.perform(get("/v1/api/inquiry/user")
                        .with(user(savedUser.getId().toString()).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].id").value(inquiry.getId()))
                .andExpect(jsonPath("$.data.content[0].productId").value(savedProduct.getId()))
                .andExpect(jsonPath("$.data.content[0].productName").value("테스트상품"))
                .andExpect(jsonPath("$.data.content[0].question").value("답변있는 질문"))
                .andExpect(jsonPath("$.data.content[0].questionCreatedAt").isNotEmpty())
                .andExpect(jsonPath("$.data.content[0].answer").value("관리자 답변"))
                .andExpect(jsonPath("$.data.content[0].answerCreatedAt").isNotEmpty());
    }

    @Test
    void 비인증_사용자_문의_조회시_401() throws Exception {
        mockMvc.perform(get("/v1/api/inquiry/user"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 문의_삭제_성공() throws Exception {
        Inquiry inquiry = inquiryRepository.save(Inquiry.builder()
                .question("삭제할 질문")
                .isPrivate(false)
                .product(savedProduct)
                .user(savedUser)
                .build());

        mockMvc.perform(delete("/v1/api/inquiry/{id}", inquiry.getId())
                        .with(user(savedUser.getId().toString()).roles("USER")))
                .andExpect(status().isOk());
    }

    @Test
    void 다른_사용자_문의_삭제시_403() throws Exception {
        User otherUser = userRepository.save(User.builder()
                .name("다른유저")
                .email("other@test.com")
                .build());
        Inquiry inquiry = inquiryRepository.save(Inquiry.builder()
                .question("다른 사람 질문")
                .isPrivate(false)
                .product(savedProduct)
                .user(otherUser)
                .build());

        mockMvc.perform(delete("/v1/api/inquiry/{id}", inquiry.getId())
                        .with(user(savedUser.getId().toString()).roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void 존재하지_않는_문의_삭제시_404() throws Exception {
        mockMvc.perform(delete("/v1/api/inquiry/{id}", 999L)
                        .with(user(savedUser.getId().toString()).roles("USER")))
                .andExpect(status().isNotFound());
    }

    @Test
    void 답변완료_문의_삭제시_400() throws Exception {
        Inquiry inquiry = inquiryRepository.save(Inquiry.builder()
                .question("답변된 질문")
                .isPrivate(false)
                .product(savedProduct)
                .user(savedUser)
                .build());
        inquiryAnswerRepository.save(InquiryAnswer.builder()
                .answer("답변입니다")
                .inquiry(inquiry)
                .build());
        em.flush();
        em.clear();

        mockMvc.perform(delete("/v1/api/inquiry/{id}", inquiry.getId())
                        .with(user(savedUser.getId().toString()).roles("USER")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 삭제된_문의_재삭제시_404() throws Exception {
        Inquiry inquiry = inquiryRepository.save(Inquiry.builder()
                .question("삭제할 질문")
                .isPrivate(false)
                .product(savedProduct)
                .user(savedUser)
                .build());

        mockMvc.perform(delete("/v1/api/inquiry/{id}", inquiry.getId())
                        .with(user(savedUser.getId().toString()).roles("USER")))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/v1/api/inquiry/{id}", inquiry.getId())
                        .with(user(savedUser.getId().toString()).roles("USER")))
                .andExpect(status().isNotFound());
    }

    @Test
    void 사용자_문의_목록_다른_유저_문의_미포함() throws Exception {
        User otherUser = userRepository.save(User.builder()
                .name("다른유저")
                .email("other@test.com")
                .build());
        inquiryRepository.save(Inquiry.builder()
                .question("내 질문")
                .isPrivate(false)
                .product(savedProduct)
                .user(savedUser)
                .build());
        inquiryRepository.save(Inquiry.builder()
                .question("다른 유저 질문")
                .isPrivate(false)
                .product(savedProduct)
                .user(otherUser)
                .build());

        mockMvc.perform(get("/v1/api/inquiry/user")
                        .with(user(savedUser.getId().toString()).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].question").value("내 질문"));
    }

    @Test
    void 상품_문의_목록_조회_답변_포함() throws Exception {
        Inquiry inquiry = inquiryRepository.save(Inquiry.builder()
                .question("답변있는 질문")
                .isPrivate(false)
                .product(savedProduct)
                .user(savedUser)
                .build());
        inquiryAnswerRepository.save(InquiryAnswer.builder()
                .answer("관리자 답변")
                .inquiry(inquiry)
                .build());
        em.flush();
        em.clear();

        mockMvc.perform(get("/v1/api/inquiry/product/{id}", savedProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].isPrivate").value(false))
                .andExpect(jsonPath("$.data.content[0].answerStatus").value("ANSWERED"))
                .andExpect(jsonPath("$.data.content[0].inquiry.id").value(inquiry.getId()))
                .andExpect(jsonPath("$.data.content[0].inquiry.question").value("답변있는 질문"))
                .andExpect(jsonPath("$.data.content[0].inquiry.userName").value("테****"))
                .andExpect(jsonPath("$.data.content[0].inquiry.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.data.content[0].inquiryAnswer.answer").value("관리자 답변"))
                .andExpect(jsonPath("$.data.content[0].inquiryAnswer.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.data.content[0].inquiryAnswer.adminName").value("관리자"));
    }

    @Test
    void 삭제된_답변_있는_문의는_미답변_필터에_포함() throws Exception {
        Inquiry inquiry = inquiryRepository.save(Inquiry.builder()
                .question("답변 삭제된 질문")
                .isPrivate(false)
                .product(savedProduct)
                .user(savedUser)
                .build());
        InquiryAnswer deletedAnswer = inquiryAnswerRepository.save(InquiryAnswer.builder()
                .answer("삭제될 답변")
                .inquiry(inquiry)
                .build());
        deletedAnswer.delete();
        inquiryAnswerRepository.save(deletedAnswer);
        em.flush();
        em.clear();

        mockMvc.perform(get("/v1/api/inquiry/product/{id}", savedProduct.getId())
                        .param("onlyUnanswered", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].answerStatus").value("UNANSWERED"))
                .andExpect(jsonPath("$.data.content[0].inquiry.question").value("답변 삭제된 질문"))
                .andExpect(jsonPath("$.data.content[0].inquiryAnswer").isEmpty());
    }

    @Test
    void 답변_삭제된_문의는_삭제_가능() throws Exception {
        Inquiry inquiry = inquiryRepository.save(Inquiry.builder()
                .question("답변 삭제된 질문")
                .isPrivate(false)
                .product(savedProduct)
                .user(savedUser)
                .build());
        InquiryAnswer deletedAnswer = inquiryAnswerRepository.save(InquiryAnswer.builder()
                .answer("삭제된 답변")
                .inquiry(inquiry)
                .build());
        deletedAnswer.delete();
        inquiryAnswerRepository.save(deletedAnswer);
        em.flush();
        em.clear();

        mockMvc.perform(delete("/v1/api/inquiry/{id}", inquiry.getId())
                        .with(user(savedUser.getId().toString()).roles("USER")))
                .andExpect(status().isOk());
    }
}
