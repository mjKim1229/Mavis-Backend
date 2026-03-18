package com.mavis.api.scheduler;

import com.mavis.domain.domains.user.repository.PasswordResetTokenRepository;
import com.mavis.domain.domains.user.repository.VerificationCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class VerificationCodeCleanupScheduler {

    private final VerificationCodeRepository verificationCodeRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Scheduled(fixedDelay = 60 * 60 * 1000)
    @Transactional
    public void cleanup() {
        LocalDateTime now = LocalDateTime.now();
        verificationCodeRepository.deleteByExpiredAtBefore(now);
        passwordResetTokenRepository.deleteByExpiredAtBefore(now);
    }
}
