package com.mavis.domain.domains.user.repository;

import com.mavis.domain.domains.user.domain.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByUsernameAndEmail(String username, String email);

    Optional<PasswordResetToken> findByToken(String token);

    void deleteByExpiredAtBefore(LocalDateTime now);
}
