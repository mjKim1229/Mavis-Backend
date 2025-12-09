package com.mavis.api.auth.service;

import com.mavis.api.auth.dto.UserPasswordFoundVerifyCodeRequest;
import com.mavis.api.auth.dto.UserPasswordFoundVerifyCreateRequest;
import com.mavis.common.util.RandomAuthCodeUtil;
import com.mavis.domain.domains.user.domain.VerificationCode;
import com.mavis.domain.domains.user.exception.InvalidVerificationCodeException;
import com.mavis.domain.domains.user.exception.VerificationCodeExpiredException;
import com.mavis.domain.domains.user.repository.VerificationCodeRepository;
import com.mavis.infrastructure.outer.email.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static com.mavis.domain.domains.user.domain.VerificationType.PASSWORD_FOUND;

@Service
@RequiredArgsConstructor
public class AuthVerificationService {
    private final VerificationCodeRepository verificationCodeRepository;
    private final MailService mailService;
    private static final long VERIFICATION_CODE_VALID_MINUTES = 5;

    @Transactional
    public void savePasswordFoundCode(UserPasswordFoundVerifyCreateRequest request) {
        Integer authCode = RandomAuthCodeUtil.generateRandomIntegerNumber();
        VerificationCode verificationCode = VerificationCode.builder()
                .code(authCode)
                .verificationType(PASSWORD_FOUND)
                .email(request.email())
                .build();
        mailService.send(request.email(), "비밀번호 찾기 인증 번호입니다.", authCode.toString());
        verificationCodeRepository.save(verificationCode);
    }

    @Transactional(readOnly = true)
    public void verifyPasswordFound(UserPasswordFoundVerifyCodeRequest request) {
        LocalDateTime validThresholdTime = LocalDateTime.now().minusMinutes(VERIFICATION_CODE_VALID_MINUTES);
        VerificationCode verificationCode = verificationCodeRepository.findByEmailAndCodeAndIsDeletedFalse(request.email(), request.code())
                .orElseThrow(() -> InvalidVerificationCodeException.EXCEPTION);
        if (verificationCode.getCreatedAt().isBefore(validThresholdTime)) {
            throw VerificationCodeExpiredException.EXCEPTION;
        }
    }
}
