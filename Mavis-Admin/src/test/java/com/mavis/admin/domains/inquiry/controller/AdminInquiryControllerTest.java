package com.mavis.admin.domains.inquiry.controller;

import com.mavis.admin.domains.inquiry.dto.CreateInquiryAnswerRequest;
import com.mavis.admin.domains.inquiry.dto.UpdateInquiryAnswerRequest;
import com.mavis.admin.support.ControllerTestSupport;
import com.mavis.domain.domains.admin.domain.Admin;
import com.mavis.domain.domains.admin.repository.AdminRepository;
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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AdminInquiryControllerTest extends ControllerTestSupport {

    @Autowired private AdminRepository adminRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private InquiryRepository inquiryRepository;
    @Autowired private InquiryAnswerRepository inquiryAnswerRepository;
    @Autowired private EntityManager em;

    private Admin savedAdmin;
    private User savedUser;
    private Product savedProduct;

    @BeforeEach
    void setUp() {
        savedAdmin = adminRepository.save(Admin.builder()
                .username("admin")
                .password("password")
                .build());
        savedUser = userRepository.save(User.builder()
                .name("테스트유저")
                .email("test@test.com")
                .build());
        savedProduct = productRepository.save(Product.builder()
                .name("테스트상품")
                .price(10000)
                .build());
    }

    @Nested
    class 문의_목록_조회 {

        @Test
        void 전체_조회_성공() throws Exception {
            Inquiry savedInquiry = inquiryRepository.save(Inquiry.builder()
                    .question("질문입니다")
                    .isPrivate(false)
                    .product(savedProduct)
                    .user(savedUser)
                    .build());

            mockMvc.perform(get("/v1/api/inquiry")
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    .andExpect(jsonPath("$.data.content[0].inquiryId").value(savedInquiry.getId()))
                    .andExpect(jsonPath("$.data.content[0].productId").value(savedProduct.getId()))
                    .andExpect(jsonPath("$.data.content[0].productName").value("테스트상품"))
                    .andExpect(jsonPath("$.data.content[0].question").value("질문입니다"))
                    .andExpect(jsonPath("$.data.content[0].userName").value("테스트유저"))
                    .andExpect(jsonPath("$.data.content[0].createdAt").isNotEmpty())
                    .andExpect(jsonPath("$.data.content[0].isAnswered").value(false));
        }

        @Test
        void 미답변_문의만_조회() throws Exception {
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
            em.flush();
            em.clear();

            mockMvc.perform(get("/v1/api/inquiry")
                            .param("status", "UNANSWERED")
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    .andExpect(jsonPath("$.data.content[0].inquiryId").value(unanswered.getId()))
                    .andExpect(jsonPath("$.data.content[0].productId").value(savedProduct.getId()))
                    .andExpect(jsonPath("$.data.content[0].productName").value("테스트상품"))
                    .andExpect(jsonPath("$.data.content[0].question").value("미답변 질문"))
                    .andExpect(jsonPath("$.data.content[0].userName").value("테스트유저"))
                    .andExpect(jsonPath("$.data.content[0].createdAt").isNotEmpty())
                    .andExpect(jsonPath("$.data.content[0].isAnswered").value(false));
        }

        @Test
        void 답변완료_문의만_조회() throws Exception {
            inquiryRepository.save(Inquiry.builder()
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
            em.flush();
            em.clear();

            mockMvc.perform(get("/v1/api/inquiry")
                            .param("status", "ANSWERED")
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    .andExpect(jsonPath("$.data.content[0].inquiryId").value(answered.getId()))
                    .andExpect(jsonPath("$.data.content[0].productId").value(savedProduct.getId()))
                    .andExpect(jsonPath("$.data.content[0].productName").value("테스트상품"))
                    .andExpect(jsonPath("$.data.content[0].question").value("답변된 질문"))
                    .andExpect(jsonPath("$.data.content[0].userName").value("테스트유저"))
                    .andExpect(jsonPath("$.data.content[0].createdAt").isNotEmpty())
                    .andExpect(jsonPath("$.data.content[0].isAnswered").value(true));
        }
    }

    @Nested
    class 문의_상세_조회 {

        @Test
        void 답변_없는_문의_조회_성공() throws Exception {
            Inquiry inquiry = inquiryRepository.save(Inquiry.builder()
                    .question("상세 질문")
                    .isPrivate(false)
                    .product(savedProduct)
                    .user(savedUser)
                    .build());

            mockMvc.perform(get("/v1/api/inquiry/{inquiryId}", inquiry.getId())
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.inquiryId").value(inquiry.getId()))
                    .andExpect(jsonPath("$.data.productId").value(savedProduct.getId()))
                    .andExpect(jsonPath("$.data.productName").value("테스트상품"))
                    .andExpect(jsonPath("$.data.productMainImageUrl").value((Object) null))
                    .andExpect(jsonPath("$.data.question").value("상세 질문"))
                    .andExpect(jsonPath("$.data.userName").value("테스트유저"))
                    .andExpect(jsonPath("$.data.createdAt").isNotEmpty())
                    .andExpect(jsonPath("$.data.isAnswered").value(false))
                    .andExpect(jsonPath("$.data.answer").value((Object) null))
                    .andExpect(jsonPath("$.data.answeredAt").value((Object) null));
        }

        @Test
        void 답변_있는_문의_조회_성공() throws Exception {
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

            mockMvc.perform(get("/v1/api/inquiry/{inquiryId}", inquiry.getId())
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.inquiryId").value(inquiry.getId()))
                    .andExpect(jsonPath("$.data.productId").value(savedProduct.getId()))
                    .andExpect(jsonPath("$.data.productName").value("테스트상품"))
                    .andExpect(jsonPath("$.data.productMainImageUrl").value((Object) null))
                    .andExpect(jsonPath("$.data.question").value("답변있는 질문"))
                    .andExpect(jsonPath("$.data.userName").value("테스트유저"))
                    .andExpect(jsonPath("$.data.createdAt").isNotEmpty())
                    .andExpect(jsonPath("$.data.isAnswered").value(true))
                    .andExpect(jsonPath("$.data.answer").value("관리자 답변"))
                    .andExpect(jsonPath("$.data.answeredAt").isNotEmpty());
        }

        @Test
        void 존재하지_않는_문의_조회시_404() throws Exception {
            mockMvc.perform(get("/v1/api/inquiry/{inquiryId}", 999L)
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class 문의_답변_등록 {

        @Test
        void 답변_등록_성공() throws Exception {
            Inquiry inquiry = inquiryRepository.save(Inquiry.builder()
                    .question("질문")
                    .isPrivate(false)
                    .product(savedProduct)
                    .user(savedUser)
                    .build());

            mockMvc.perform(post("/v1/api/inquiry/answer/{inquiryId}", inquiry.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new CreateInquiryAnswerRequest("답변입니다")))
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isOk());
        }

        @Test
        void 이미_답변된_문의에_답변_등록시_400() throws Exception {
            Inquiry inquiry = inquiryRepository.save(Inquiry.builder()
                    .question("질문")
                    .isPrivate(false)
                    .product(savedProduct)
                    .user(savedUser)
                    .build());
            inquiryAnswerRepository.save(InquiryAnswer.builder()
                    .answer("기존 답변")
                    .inquiry(inquiry)
                    .build());
            em.flush();
            em.clear();

            mockMvc.perform(post("/v1/api/inquiry/answer/{inquiryId}", inquiry.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new CreateInquiryAnswerRequest("중복 답변")))
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void 존재하지_않는_문의에_답변_등록시_404() throws Exception {
            mockMvc.perform(post("/v1/api/inquiry/answer/{inquiryId}", 999L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new CreateInquiryAnswerRequest("답변입니다")))
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class 문의_답변_수정 {

        @Test
        void 답변_수정_성공() throws Exception {
            Inquiry inquiry = inquiryRepository.save(Inquiry.builder()
                    .question("질문")
                    .isPrivate(false)
                    .product(savedProduct)
                    .user(savedUser)
                    .build());
            inquiryAnswerRepository.save(InquiryAnswer.builder()
                    .answer("기존 답변")
                    .inquiry(inquiry)
                    .build());
            em.flush();
            em.clear();

            mockMvc.perform(put("/v1/api/inquiry/answer/{inquiryId}", inquiry.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new UpdateInquiryAnswerRequest("수정된 답변")))
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isOk());
        }

        @Test
        void 존재하지_않는_문의에_답변_수정시_404() throws Exception {
            mockMvc.perform(put("/v1/api/inquiry/answer/{inquiryId}", 999L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new UpdateInquiryAnswerRequest("수정된 답변")))
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isNotFound());
        }

        @Test
        void 답변_없는_문의에_답변_수정시_404() throws Exception {
            Inquiry inquiry = inquiryRepository.save(Inquiry.builder()
                    .question("질문")
                    .isPrivate(false)
                    .product(savedProduct)
                    .user(savedUser)
                    .build());

            mockMvc.perform(put("/v1/api/inquiry/answer/{inquiryId}", inquiry.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new UpdateInquiryAnswerRequest("수정된 답변")))
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class 문의_답변_삭제 {

        @Test
        void 답변_삭제_성공() throws Exception {
            Inquiry inquiry = inquiryRepository.save(Inquiry.builder()
                    .question("질문")
                    .isPrivate(false)
                    .product(savedProduct)
                    .user(savedUser)
                    .build());
            inquiryAnswerRepository.save(InquiryAnswer.builder()
                    .answer("답변")
                    .inquiry(inquiry)
                    .build());
            em.flush();
            em.clear();

            mockMvc.perform(delete("/v1/api/inquiry/answer/{inquiryId}", inquiry.getId())
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isOk());
        }

        @Test
        void 존재하지_않는_문의에_답변_삭제시_404() throws Exception {
            mockMvc.perform(delete("/v1/api/inquiry/answer/{inquiryId}", 999L)
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isNotFound());
        }

        @Test
        void 답변_없는_문의에_답변_삭제시_404() throws Exception {
            Inquiry inquiry = inquiryRepository.save(Inquiry.builder()
                    .question("질문")
                    .isPrivate(false)
                    .product(savedProduct)
                    .user(savedUser)
                    .build());

            mockMvc.perform(delete("/v1/api/inquiry/answer/{inquiryId}", inquiry.getId())
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isNotFound());
        }
    }

    @Test
    void 비인증_요청시_401() throws Exception {
        mockMvc.perform(get("/v1/api/inquiry"))
                .andExpect(status().isUnauthorized());
    }
}
