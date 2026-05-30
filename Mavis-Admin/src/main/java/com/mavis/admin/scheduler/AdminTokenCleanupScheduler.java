package com.mavis.admin.scheduler;

import com.mavis.domain.domains.admin.repository.AdminPasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class AdminTokenCleanupScheduler {

    private final AdminPasswordResetTokenRepository adminPasswordResetTokenRepository;

    @Scheduled(fixedDelay = 24 * 60 * 60 * 1000)
    @Transactional
    public void cleanup() {
        adminPasswordResetTokenRepository.deleteByExpiredAtBefore(LocalDateTime.now());
    }
}
