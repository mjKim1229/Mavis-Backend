package com.mavis.api.auth.service;

import com.mavis.api.auth.dto.UserPasswordFoundVerifyCodeRequest;
import com.mavis.api.auth.dto.UserPasswordFoundVerifyCreateRequest;
import com.mavis.api.auth.dto.UserSignUpCodeCreateRequest;
import com.mavis.api.auth.dto.UserSignUpCodeVerifyRequest;
import com.mavis.api.auth.dto.UserEmailChangeCreateRequest;
import com.mavis.api.auth.dto.UserEmailChangeVerifyRequest;
import com.mavis.api.auth.implement.UserReader;
import com.mavis.common.util.RandomAuthCodeUtil;
import com.mavis.domain.domains.user.domain.VerificationCode;
import com.mavis.domain.domains.user.domain.VerificationType;
import com.mavis.domain.domains.user.domain.User;
import com.mavis.domain.domains.user.exception.InvalidVerificationCodeException;
import com.mavis.domain.domains.user.exception.VerificationCodeExpiredException;
import com.mavis.domain.domains.user.repository.UserRepository;
import com.mavis.domain.domains.user.repository.VerificationCodeRepository;
import com.mavis.infrastructure.outer.email.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static com.mavis.domain.domains.user.domain.VerificationType.PASSWORD_FOUND;
import static com.mavis.domain.domains.user.domain.VerificationType.SIGN_UP;
import static com.mavis.domain.domains.user.domain.VerificationType.UPDATE_EMAIL;

@Service
@RequiredArgsConstructor
public class AuthVerificationService {
    private final VerificationCodeRepository verificationCodeRepository;
    private final MailService mailService;
    private static final long VERIFICATION_CODE_VALID_MINUTES = 5;
    private final UserRepository userRepository;
    private final UserReader userReader;

    @Transactional
    public void savePasswordFoundCode(UserPasswordFoundVerifyCreateRequest request) {
        boolean isUserExists = userRepository.existsByEmailAndIsDeletedFalse(request.email());
        if (!isUserExists) return;

        Integer authCode = RandomAuthCodeUtil.generateRandomIntegerNumber();
        LocalDateTime expiredAt = LocalDateTime.now().plusMinutes(VERIFICATION_CODE_VALID_MINUTES);
        verificationCodeRepository.findByVerificationTypeAndEmail(PASSWORD_FOUND, request.email())
                .ifPresentOrElse(
                        verificationCode -> verificationCode.update(authCode, expiredAt)
                        , () -> savePasswordCode(request, authCode, expiredAt)
                );
        mailService.send(request.email(), "비밀번호 찾기 인증 번호입니다.", authCode.toString());
    }

    private void savePasswordCode(UserPasswordFoundVerifyCreateRequest request, Integer authCode, LocalDateTime expiredAt) {
        VerificationCode verificationCode = VerificationCode.builder()
                .code(authCode)
                .verificationType(PASSWORD_FOUND)
                .expiredAt(expiredAt)
                .email(request.email())
                .build();
        verificationCodeRepository.save(verificationCode);
    }

    @Transactional
    public void verifyPasswordFound(UserPasswordFoundVerifyCodeRequest request) {
        VerificationCode verificationCode = verificationCodeRepository.findByVerificationTypeAndEmailAndCodeAndIsDeletedFalse(VerificationType.PASSWORD_FOUND, request.email(), request.code())
                .orElseThrow(() -> InvalidVerificationCodeException.EXCEPTION);
        if (verificationCode.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw VerificationCodeExpiredException.EXCEPTION;
        }
        verificationCodeRepository.delete(verificationCode);
    }

    @Transactional
    public void saveSignUpCode(UserSignUpCodeCreateRequest request) {
        Integer authCode = RandomAuthCodeUtil.generateRandomIntegerNumber();
        LocalDateTime expiredAt = LocalDateTime.now().plusMinutes(VERIFICATION_CODE_VALID_MINUTES);
        verificationCodeRepository.findByVerificationTypeAndEmail(SIGN_UP, request.email())
                .ifPresentOrElse(verificationCode -> verificationCode.update(authCode, expiredAt)
                        , () -> saveSignUpAuthCode(request, authCode, expiredAt)
                );
        mailService.send(request.email(), "회원가입 인증 번호입니다.", authCode.toString());
    }

    private void saveSignUpAuthCode(UserSignUpCodeCreateRequest request, Integer authCode, LocalDateTime expiredAt) {
        VerificationCode verificationCode = VerificationCode.builder()
                .code(authCode)
                .verificationType(SIGN_UP)
                .expiredAt(expiredAt)
                .email(request.email())
                .build();

        verificationCodeRepository.save(verificationCode);
    }

    @Transactional
    public void verifySignUpFound(UserSignUpCodeVerifyRequest request) {
        VerificationCode verificationCode = verificationCodeRepository.findByVerificationTypeAndEmailAndCodeAndIsDeletedFalse(SIGN_UP, request.email(), request.code())
                .orElseThrow(() -> InvalidVerificationCodeException.EXCEPTION);
        if (verificationCode.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw VerificationCodeExpiredException.EXCEPTION;
        }
        verificationCodeRepository.delete(verificationCode);
    }

    @Transactional
    public void saveEmailChangeCode(UserEmailChangeCreateRequest request) {
        Integer authCode = RandomAuthCodeUtil.generateRandomIntegerNumber();
        LocalDateTime expiredAt = LocalDateTime.now().plusMinutes(VERIFICATION_CODE_VALID_MINUTES);
        verificationCodeRepository.findByVerificationTypeAndEmail(UPDATE_EMAIL, request.newEmail())
                .ifPresentOrElse(verificationCode -> verificationCode.update(authCode, expiredAt)
                        , () -> saveEmailChangeAuthCode(request, authCode, expiredAt)
                );
        mailService.send(request.newEmail(), "이메일 변경 인증 번호입니다.", authCode.toString());
    }

    private void saveEmailChangeAuthCode(UserEmailChangeCreateRequest request, Integer authCode, LocalDateTime expiredAt) {
        VerificationCode verificationCode = VerificationCode.builder()
                .code(authCode)
                .verificationType(UPDATE_EMAIL)
                .expiredAt(expiredAt)
                .email(request.newEmail())
                .build();
        verificationCodeRepository.save(verificationCode);
    }

    @Transactional
    public void verifyEmailChange(UserEmailChangeVerifyRequest request) {
        VerificationCode verificationCode = verificationCodeRepository.findByVerificationTypeAndEmailAndCodeAndIsDeletedFalse(UPDATE_EMAIL, request.newEmail(), request.code())
                .orElseThrow(() -> InvalidVerificationCodeException.EXCEPTION);
        if (verificationCode.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw VerificationCodeExpiredException.EXCEPTION;
        }

        User user = userReader.getCurrentUser();
        user.changeEmail(request.newEmail());

        verificationCodeRepository.delete(verificationCode);
    }
}
