package com.mavis.domain.domains.user.repository;

import com.mavis.domain.domains.user.domain.VerificationCode;
import com.mavis.domain.domains.user.domain.VerificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {
    Optional<VerificationCode> findByVerificationTypeAndEmail(VerificationType verificationType, String email);
    Optional<VerificationCode> findByVerificationTypeAndEmailAndCodeAndIsDeletedFalse(VerificationType verificationType, String email, Integer code);

    void deleteByExpiredAtBefore(LocalDateTime nowTime);
}
