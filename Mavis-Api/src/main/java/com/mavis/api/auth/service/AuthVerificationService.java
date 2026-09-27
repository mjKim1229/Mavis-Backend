package com.mavis.api.auth.service;

import com.mavis.api.auth.dto.*;
import com.mavis.api.auth.implement.UserReader;
import com.mavis.infrastructure.outer.email.event.FindUsernameMailEvent;
import com.mavis.common.util.RandomAuthCodeUtil;
import com.mavis.domain.domains.user.domain.PasswordResetToken;
import com.mavis.domain.domains.user.domain.User;
import com.mavis.domain.domains.user.domain.VerificationCode;
import com.mavis.domain.domains.user.domain.VerificationType;
import com.mavis.domain.domains.user.exception.DuplicateEmailException;
import com.mavis.domain.domains.user.exception.InvalidVerificationCodeException;
import com.mavis.domain.domains.user.exception.UserNotFoundException;
import com.mavis.domain.domains.user.exception.VerificationAttemptsExceededException;
import com.mavis.domain.domains.user.exception.VerificationCodeExpiredException;
import com.mavis.domain.domains.user.repository.PasswordResetTokenRepository;
import com.mavis.domain.domains.user.repository.UserRepository;
import com.mavis.domain.domains.user.repository.VerificationCodeRepository;
import com.mavis.infrastructure.outer.email.event.PasswordResetMailEvent;
import com.mavis.infrastructure.outer.email.event.VerifyMailEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static com.mavis.domain.domains.user.domain.SnsType.MANUAL;
import static com.mavis.domain.domains.user.domain.VerificationType.SIGN_UP;
import static com.mavis.domain.domains.user.domain.VerificationType.UPDATE_EMAIL;

@Service
@RequiredArgsConstructor
public class AuthVerificationService {
    private static final String PASSWORD_RESET_URL = "https://www.garamall.com/password-reset?token=";
    private static final long VERIFICATION_CODE_VALID_MINUTES = 5;
    private static final long SIGN_UP_COMPLETION_VALID_MINUTES = 30;
    private static final long PASSWORD_RESET_LINK_VALID_MINUTES = 10;

    private final VerificationCodeRepository verificationCodeRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserRepository userRepository;
    private final UserReader userReader;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void savePasswordFoundCode(UserPasswordFoundVerifyCreateRequest request) {
        boolean isUserExists = userRepository.existsByUsernameAndEmailAndIsDeletedFalse(request.username(), request.email());
        if (!isUserExists) return;

        UUID token = UUID.randomUUID();
        String resetURL = PASSWORD_RESET_URL + token;

        LocalDateTime expiredAt = LocalDateTime.now().plusMinutes(PASSWORD_RESET_LINK_VALID_MINUTES);
        passwordResetTokenRepository.findByUsernameAndEmail(request.username(), request.email())
                .ifPresentOrElse(
                        passwordResetToken -> passwordResetToken.update(token.toString(), expiredAt)
                        , () -> saveToken(request, token.toString(), expiredAt)
                );
        eventPublisher.publishEvent(new PasswordResetMailEvent(request.email(), resetURL));
    }

    private void saveToken(UserPasswordFoundVerifyCreateRequest request, String token, LocalDateTime expiredAt) {
        PasswordResetToken passwordResetToken = PasswordResetToken.builder()
                .username(request.username())
                .email(request.email())
                .expiredAt(expiredAt)
                .token(token)
                .build();
        passwordResetTokenRepository.save(passwordResetToken);
    }

    @Transactional
    public void verifyPasswordFound(UserPasswordFoundVerifyCodeRequest request) {
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(request.token())
                .orElseThrow(() -> InvalidVerificationCodeException.EXCEPTION);

        if (passwordResetToken.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw VerificationCodeExpiredException.EXCEPTION;
        }

        User user = userRepository.findByUsernameAndIsDeletedFalse(passwordResetToken.getUsername())
                .orElseThrow(() -> UserNotFoundException.EXCEPTION);
        String password = passwordEncoder.encode(request.password());
        user.updatePassword(password);
        passwordResetTokenRepository.delete(passwordResetToken);
    }

    @Transactional
    public void findUsername(UserFindUsernameRequest request) {
        userRepository.findBySnsTypeAndEmailAndIsDeletedFalse(MANUAL, request.email())
                .ifPresent(user -> eventPublisher.publishEvent(new FindUsernameMailEvent(request.email(), user.getUsername())));
    }

    @Transactional
    public void saveSignUpCode(UserSignUpCodeCreateRequest request) {
        if (userRepository.existsBySnsTypeAndEmailAndIsDeletedFalse(MANUAL, request.email())) {
            throw DuplicateEmailException.EXCEPTION;
        }
        Integer authCode = issueCode(SIGN_UP, request.email());
        eventPublisher.publishEvent(new VerifyMailEvent(request.email(), "[가람몰] 회원가입 인증번호 안내", authCode.toString()));
    }

    @Transactional(noRollbackFor = {InvalidVerificationCodeException.class, VerificationAttemptsExceededException.class})
    public void verifySignUpFound(UserSignUpCodeVerifyRequest request) {
        VerificationCode verificationCode = verificationCodeRepository.findByVerificationTypeAndEmail(SIGN_UP, request.email())
                .orElseThrow(() -> InvalidVerificationCodeException.EXCEPTION);
        LocalDateTime now = LocalDateTime.now();
        verificationCode.matchCode(request.code(), now);

        LocalDateTime signUpDeadline = now.plusMinutes(SIGN_UP_COMPLETION_VALID_MINUTES);
        verificationCode.verify(signUpDeadline);
    }

    @Transactional
    public void saveEmailChangeCode(UserEmailChangeCreateRequest request) {
        if (userRepository.existsBySnsTypeAndEmailAndIsDeletedFalse(MANUAL, request.newEmail())) {
            throw DuplicateEmailException.EXCEPTION;
        }
        Integer authCode = issueCode(UPDATE_EMAIL, request.newEmail());
        eventPublisher.publishEvent(new VerifyMailEvent(request.newEmail(), "[가람몰] 이메일 변경 인증번호 안내", authCode.toString()));
    }

    @Transactional(noRollbackFor = {InvalidVerificationCodeException.class, VerificationAttemptsExceededException.class})
    public void verifyEmailChange(UserEmailChangeVerifyRequest request) {
        VerificationCode verificationCode = verificationCodeRepository.findByVerificationTypeAndEmail(UPDATE_EMAIL, request.newEmail())
                .orElseThrow(() -> InvalidVerificationCodeException.EXCEPTION);
        LocalDateTime now = LocalDateTime.now();
        verificationCode.matchCode(request.code(), now);
        if (userRepository.existsBySnsTypeAndEmailAndIsDeletedFalse(MANUAL, request.newEmail())) {
            throw DuplicateEmailException.EXCEPTION;
        }

        User user = userReader.getCurrentUser();
        user.changeEmail(request.newEmail());

        verificationCodeRepository.delete(verificationCode);
    }

    private Integer issueCode(VerificationType verificationType, String email) {
        Integer authCode = RandomAuthCodeUtil.generateRandomIntegerNumber();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiredAt = now.plusMinutes(VERIFICATION_CODE_VALID_MINUTES);
        Optional<VerificationCode> existing = verificationCodeRepository.findByVerificationTypeAndEmail(verificationType, email);
        if (existing.isPresent()) {
            VerificationCode verificationCode = existing.get();
            verificationCode.reissue(authCode, expiredAt, now);
        } else {
            VerificationCode verificationCode = VerificationCode.issue(verificationType, email, authCode, expiredAt, now);
            verificationCodeRepository.save(verificationCode);
        }
        return authCode;
    }
}
