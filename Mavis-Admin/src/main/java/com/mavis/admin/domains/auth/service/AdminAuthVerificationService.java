package com.mavis.admin.domains.auth.service;

import com.mavis.admin.domains.admin.implement.AdminReader;
import com.mavis.admin.domains.auth.dto.AdminPasswordChangeRequest;
import com.mavis.admin.domains.auth.dto.AdminPasswordResetConfirmRequest;
import com.mavis.admin.domains.auth.dto.AdminPasswordResetEmailRequest;
import com.mavis.domain.domains.admin.domain.Admin;
import com.mavis.domain.domains.admin.domain.AdminPasswordResetToken;
import com.mavis.domain.domains.admin.exception.AdminInvalidPasswordException;
import com.mavis.domain.domains.admin.exception.AdminNotFoundException;
import com.mavis.domain.domains.admin.exception.AdminResetTokenExpiredException;
import com.mavis.domain.domains.admin.exception.AdminResetTokenInvalidException;
import com.mavis.domain.domains.admin.repository.AdminPasswordResetTokenRepository;
import com.mavis.domain.domains.admin.repository.AdminRepository;
import com.mavis.infrastructure.outer.email.event.PasswordResetMailEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminAuthVerificationService {

    private static final String PASSWORD_RESET_URL = "https://www.garamall.com/admin/password-reset?token=";
    private static final long PASSWORD_RESET_LINK_VALID_MINUTES = 10;

    private final AdminRepository adminRepository;
    private final AdminPasswordResetTokenRepository adminPasswordResetTokenRepository;
    private final AdminReader adminReader;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void sendPasswordResetEmail(AdminPasswordResetEmailRequest request) {
        if (!adminRepository.existsByEmailAndIsDeletedFalse(request.email())) return;

        UUID token = UUID.randomUUID();
        String resetUrl = PASSWORD_RESET_URL + token;
        LocalDateTime expiredAt = LocalDateTime.now().plusMinutes(PASSWORD_RESET_LINK_VALID_MINUTES);

        adminPasswordResetTokenRepository.findByEmail(request.email())
                .ifPresentOrElse(
                        resetToken -> resetToken.update(token.toString(), expiredAt),
                        () -> saveResetToken(request.email(), token.toString(), expiredAt)
                );

        eventPublisher.publishEvent(new PasswordResetMailEvent(request.email(), resetUrl));
    }

    private void saveResetToken(String email, String token, LocalDateTime expiredAt) {
        AdminPasswordResetToken resetToken = AdminPasswordResetToken.builder()
                .email(email)
                .token(token)
                .expiredAt(expiredAt)
                .build();
        adminPasswordResetTokenRepository.save(resetToken);
    }

    @Transactional
    public void confirmPasswordReset(AdminPasswordResetConfirmRequest request) {
        AdminPasswordResetToken resetToken = adminPasswordResetTokenRepository.findByToken(request.token())
                .orElseThrow(() -> AdminResetTokenInvalidException.EXCEPTION);

        if (resetToken.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw AdminResetTokenExpiredException.EXCEPTION;
        }

        Admin admin = adminRepository.findByEmailAndIsDeletedFalse(resetToken.getEmail())
                .orElseThrow(() -> AdminNotFoundException.EXCEPTION);

        admin.updatePassword(passwordEncoder.encode(request.newPassword()));
        adminPasswordResetTokenRepository.delete(resetToken);
    }

    @Transactional
    public void changePassword(AdminPasswordChangeRequest request) {
        Admin admin = adminReader.getCurrentAdmin();

        if (!passwordEncoder.matches(request.currentPassword(), admin.getPassword())) {
            throw AdminInvalidPasswordException.EXCEPTION;
        }

        admin.updatePassword(passwordEncoder.encode(request.newPassword()));
    }
}
