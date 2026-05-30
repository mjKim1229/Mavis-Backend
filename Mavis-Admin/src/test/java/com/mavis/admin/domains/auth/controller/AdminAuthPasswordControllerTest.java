package com.mavis.admin.domains.auth.controller;

import com.mavis.admin.domains.auth.dto.AdminPasswordChangeRequest;
import com.mavis.admin.domains.auth.dto.AdminPasswordResetConfirmRequest;
import com.mavis.admin.domains.auth.dto.AdminPasswordResetEmailRequest;
import com.mavis.admin.support.ControllerTestSupport;
import com.mavis.domain.domains.admin.domain.Admin;
import com.mavis.domain.domains.admin.domain.AdminPasswordResetToken;
import com.mavis.domain.domains.admin.repository.AdminPasswordResetTokenRepository;
import com.mavis.domain.domains.admin.repository.AdminRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminAuthPasswordControllerTest extends ControllerTestSupport {

    @Autowired private AdminRepository adminRepository;
    @Autowired private AdminPasswordResetTokenRepository adminPasswordResetTokenRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private Admin savedAdmin;
    private static final String ADMIN_EMAIL = "admin@garamall.com";
    private static final String ADMIN_RAW_PASSWORD = "password1234";

    @BeforeEach
    void setUp() {
        savedAdmin = adminRepository.save(Admin.builder()
                .username("admin")
                .password(passwordEncoder.encode(ADMIN_RAW_PASSWORD))
                .email(ADMIN_EMAIL)
                .build());
    }

    @Nested
    class 비밀번호_재설정_메일_발송 {

        @Test
        void 존재하는_이메일이면_토큰_저장_후_200() throws Exception {
            mockMvc.perform(post("/v1/api/admin/auth/password/reset")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new AdminPasswordResetEmailRequest(ADMIN_EMAIL))))
                    .andExpect(status().isOk());

            assertThat(adminPasswordResetTokenRepository.findByEmail(ADMIN_EMAIL)).isPresent();
        }

        @Test
        void 존재하지_않는_이메일이면_토큰_저장_없이_200() throws Exception {
            mockMvc.perform(post("/v1/api/admin/auth/password/reset")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new AdminPasswordResetEmailRequest("unknown@garamall.com"))))
                    .andExpect(status().isOk());

            assertThat(adminPasswordResetTokenRepository.findByEmail("unknown@garamall.com")).isEmpty();
        }

        @Test
        void 재요청시_토큰_갱신() throws Exception {
            adminPasswordResetTokenRepository.save(AdminPasswordResetToken.builder()
                    .email(ADMIN_EMAIL)
                    .token("old-token")
                    .expiredAt(LocalDateTime.now().plusMinutes(10))
                    .build());

            mockMvc.perform(post("/v1/api/admin/auth/password/reset")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new AdminPasswordResetEmailRequest(ADMIN_EMAIL))))
                    .andExpect(status().isOk());

            AdminPasswordResetToken token = adminPasswordResetTokenRepository.findByEmail(ADMIN_EMAIL).get();
            assertThat(token.getToken()).isNotEqualTo("old-token");
        }
    }

    @Nested
    class 비밀번호_재설정_확인 {

        @Test
        void 유효한_토큰이면_비밀번호_변경_후_200() throws Exception {
            String token = "valid-token";
            adminPasswordResetTokenRepository.save(AdminPasswordResetToken.builder()
                    .email(ADMIN_EMAIL)
                    .token(token)
                    .expiredAt(LocalDateTime.now().plusMinutes(10))
                    .build());

            mockMvc.perform(put("/v1/api/admin/auth/password/reset")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new AdminPasswordResetConfirmRequest(token, "newPassword1234"))))
                    .andExpect(status().isOk());

            Admin updatedAdmin = adminRepository.findByEmailAndIsDeletedFalse(ADMIN_EMAIL).get();
            assertThat(passwordEncoder.matches("newPassword1234", updatedAdmin.getPassword())).isTrue();
            assertThat(adminPasswordResetTokenRepository.findByToken(token)).isEmpty();
        }

        @Test
        void 존재하지_않는_토큰이면_400() throws Exception {
            mockMvc.perform(put("/v1/api/admin/auth/password/reset")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new AdminPasswordResetConfirmRequest("nonexistent-token", "newPassword1234"))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void 만료된_토큰이면_400() throws Exception {
            String token = "expired-token";
            adminPasswordResetTokenRepository.save(AdminPasswordResetToken.builder()
                    .email(ADMIN_EMAIL)
                    .token(token)
                    .expiredAt(LocalDateTime.now().minusMinutes(1))
                    .build());

            mockMvc.perform(put("/v1/api/admin/auth/password/reset")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new AdminPasswordResetConfirmRequest(token, "newPassword1234"))))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class 비밀번호_변경_로그인상태 {

        @Test
        void 올바른_현재_비밀번호이면_변경_후_200() throws Exception {
            mockMvc.perform(put("/v1/api/admin/auth/password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new AdminPasswordChangeRequest(ADMIN_RAW_PASSWORD, "changedPassword1234")))
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isOk());

            Admin updatedAdmin = adminRepository.findByIdAndIsDeletedFalse(savedAdmin.getId()).get();
            assertThat(passwordEncoder.matches("changedPassword1234", updatedAdmin.getPassword())).isTrue();
        }

        @Test
        void 틀린_현재_비밀번호이면_401() throws Exception {
            mockMvc.perform(put("/v1/api/admin/auth/password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new AdminPasswordChangeRequest("wrongPassword", "changedPassword1234")))
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void 비인증_요청이면_401() throws Exception {
            mockMvc.perform(put("/v1/api/admin/auth/password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new AdminPasswordChangeRequest(ADMIN_RAW_PASSWORD, "changedPassword1234"))))
                    .andExpect(status().isUnauthorized());
        }
    }
}
