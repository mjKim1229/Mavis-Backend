package com.mavis.domain.domains.admin.repository;

import com.mavis.domain.domains.admin.domain.AdminPasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface AdminPasswordResetTokenRepository extends JpaRepository<AdminPasswordResetToken, Long> {

    Optional<AdminPasswordResetToken> findByEmail(String email);

    Optional<AdminPasswordResetToken> findByToken(String token);

    void deleteByExpiredAtBefore(LocalDateTime dateTime);
}
