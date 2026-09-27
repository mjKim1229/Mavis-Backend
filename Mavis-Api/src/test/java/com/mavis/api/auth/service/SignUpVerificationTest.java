package com.mavis.api.auth.service;

import com.mavis.api.auth.dto.UserSignUpCodeCreateRequest;
import com.mavis.api.auth.dto.UserEmailChangeCreateRequest;
import com.mavis.api.auth.dto.UserEmailChangeVerifyRequest;
import com.mavis.api.auth.dto.UserSignUpCodeVerifyRequest;
import com.mavis.api.auth.dto.UserSignUpRequest;
import com.mavis.api.support.ControllerTestSupport;
import com.mavis.domain.domains.user.domain.Gender;
import com.mavis.domain.domains.user.domain.SnsType;
import com.mavis.domain.domains.user.domain.User;
import com.mavis.domain.domains.user.domain.VerificationCode;
import com.mavis.domain.domains.user.domain.VerificationType;
import com.mavis.domain.domains.user.exception.DuplicateEmailException;
import com.mavis.domain.domains.user.exception.DuplicateUsernameException;
import com.mavis.domain.domains.user.exception.EmailNotVerifiedException;
import com.mavis.domain.domains.user.exception.InvalidVerificationCodeException;
import com.mavis.domain.domains.user.exception.VerificationAttemptsExceededException;
import com.mavis.domain.domains.user.repository.UserRepository;
import com.mavis.domain.domains.user.repository.VerificationCodeRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SignUpVerificationTest extends ControllerTestSupport {

    private static final String EMAIL = "signup-test@test.com";
    private static final int ISSUED_CODE = 123456;
    private static final int WRONG_CODE = 111111;

    @Autowired private UserService userService;
    @Autowired private AuthVerificationService authVerificationService;
    @Autowired private UserRepository userRepository;
    @Autowired private VerificationCodeRepository verificationCodeRepository;
    @Autowired private EntityManager em;

    private UserSignUpRequest signUpRequest() {
        return new UserSignUpRequest(
                "testuser", "password123!", "닉네임", "홍길동",
                Gender.MALE, LocalDate.of(1995, 1, 1), "01012345678",
                EMAIL, true, true);
    }

    private Integer issueCode() {
        authVerificationService.saveSignUpCode(new UserSignUpCodeCreateRequest(EMAIL));
        em.flush();
        em.clear();
        VerificationCode saved = verificationCodeRepository
                .findByVerificationTypeAndEmail(VerificationType.SIGN_UP, EMAIL)
                .orElseThrow();
        return saved.getCode();
    }

    @Test
    void 이메일_인증을_건너뛰고_가입하면_EmailNotVerifiedException() {
        assertThatThrownBy(() -> userService.signUp(signUpRequest()))
                .isInstanceOf(EmailNotVerifiedException.class);

        assertThat(userRepository.existsBySnsTypeAndEmailAndIsDeletedFalse(SnsType.MANUAL, EMAIL)).isFalse();
    }

    @Test
    void 인증번호만_발급받고_확인하지_않으면_가입할_수_없다() {
        issueCode();

        assertThatThrownBy(() -> userService.signUp(signUpRequest()))
                .isInstanceOf(EmailNotVerifiedException.class);
    }

    @Test
    void 인증번호_확인_후에는_가입할_수_있다() {
        Integer code = issueCode();

        authVerificationService.verifySignUpFound(new UserSignUpCodeVerifyRequest(EMAIL, code));
        em.flush();
        em.clear();

        userService.signUp(signUpRequest());
        em.flush();
        em.clear();

        assertThat(userRepository.existsBySnsTypeAndEmailAndIsDeletedFalse(SnsType.MANUAL, EMAIL)).isTrue();
    }

    @Test
    void 가입에_사용된_인증정보는_재사용할_수_없다() {
        Integer code = issueCode();
        authVerificationService.verifySignUpFound(new UserSignUpCodeVerifyRequest(EMAIL, code));
        userService.signUp(signUpRequest());
        em.flush();
        em.clear();

        // 같은 이메일로 다시 가입 시도 — 인증 정보가 소비되어 남아있지 않다
        assertThat(verificationCodeRepository.findByVerificationTypeAndEmail(VerificationType.SIGN_UP, EMAIL)).isEmpty();
    }

    @Test
    void 틀린_인증번호로는_인증되지_않는다() {
        Integer code = issueCode();
        int wrongCode = code + 1;

        assertThatThrownBy(() ->
                authVerificationService.verifySignUpFound(new UserSignUpCodeVerifyRequest(EMAIL, wrongCode)))
                .isInstanceOf(InvalidVerificationCodeException.class);
    }

    @Test
    void 인증_후_유효시간이_지나면_가입할_수_없다() {
        Integer code = issueCode();
        authVerificationService.verifySignUpFound(new UserSignUpCodeVerifyRequest(EMAIL, code));
        em.flush();
        em.clear();

        VerificationCode verified = verificationCodeRepository
                .findByVerificationTypeAndEmail(VerificationType.SIGN_UP, EMAIL)
                .orElseThrow();
        verified.verify(LocalDateTime.now().minusMinutes(1)); // 만료 상태로 조정
        em.flush();
        em.clear();

        assertThatThrownBy(() -> userService.signUp(signUpRequest()))
                .isInstanceOf(EmailNotVerifiedException.class);
    }

    @Test
    void 활성_사용자가_쓰는_아이디로는_가입할_수_없다() {
        userRepository.save(User.builder()
                .username("testuser")
                .email("other@test.com")
                .snsType(SnsType.MANUAL)
                .build());
        Integer code = issueCode();
        authVerificationService.verifySignUpFound(new UserSignUpCodeVerifyRequest(EMAIL, code));
        em.flush();
        em.clear();

        assertThatThrownBy(() -> userService.signUp(signUpRequest()))
                .isInstanceOf(DuplicateUsernameException.class);
    }

    @Test
    void 탈퇴한_사용자의_아이디로는_가입할_수_있다() {
        User withdrawn = userRepository.save(User.builder()
                .username("testuser")
                .email("other@test.com")
                .snsType(SnsType.MANUAL)
                .build());
        withdrawn.withDraw();
        Integer code = issueCode();
        authVerificationService.verifySignUpFound(new UserSignUpCodeVerifyRequest(EMAIL, code));
        em.flush();
        em.clear();

        userService.signUp(signUpRequest());
        em.flush();
        em.clear();

        assertThat(userRepository.existsByUsernameAndIsDeletedFalse("testuser")).isTrue();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void 가입_인증번호를_틀리면_예외가_나도_틀린_횟수는_DB에_남는다() {
        String email = "rollback-signup-" + UUID.randomUUID() + "@test.com";
        saveIssuedCode(VerificationType.SIGN_UP, email);
        try {
            assertThatThrownBy(() ->
                    authVerificationService.verifySignUpFound(new UserSignUpCodeVerifyRequest(email, WRONG_CODE)))
                    .isInstanceOf(InvalidVerificationCodeException.class);

            assertThat(reloadFailedAttempts(VerificationType.SIGN_UP, email)).isEqualTo(1);
        } finally {
            deleteCode(VerificationType.SIGN_UP, email);
        }
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void 가입_인증번호를_5번_틀리면_입력횟수_초과이고_횟수가_DB에_남는다() {
        String email = "rollback-exceeded-" + UUID.randomUUID() + "@test.com";
        saveIssuedCode(VerificationType.SIGN_UP, email);
        try {
            for (int i = 0; i < 4; i++) {
                assertThatThrownBy(() ->
                        authVerificationService.verifySignUpFound(new UserSignUpCodeVerifyRequest(email, WRONG_CODE)))
                        .isInstanceOf(InvalidVerificationCodeException.class);
            }
            assertThatThrownBy(() ->
                    authVerificationService.verifySignUpFound(new UserSignUpCodeVerifyRequest(email, WRONG_CODE)))
                    .isInstanceOf(VerificationAttemptsExceededException.class);

            assertThat(reloadFailedAttempts(VerificationType.SIGN_UP, email)).isEqualTo(5);
            assertThatThrownBy(() ->
                    authVerificationService.verifySignUpFound(new UserSignUpCodeVerifyRequest(email, ISSUED_CODE)))
                    .isInstanceOf(VerificationAttemptsExceededException.class);
        } finally {
            deleteCode(VerificationType.SIGN_UP, email);
        }
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void 이메일_변경_인증번호를_틀리면_예외가_나도_틀린_횟수는_DB에_남는다() {
        String email = "rollback-change-" + UUID.randomUUID() + "@test.com";
        saveIssuedCode(VerificationType.UPDATE_EMAIL, email);
        try {
            assertThatThrownBy(() ->
                    authVerificationService.verifyEmailChange(new UserEmailChangeVerifyRequest(email, WRONG_CODE)))
                    .isInstanceOf(InvalidVerificationCodeException.class);

            assertThat(reloadFailedAttempts(VerificationType.UPDATE_EMAIL, email)).isEqualTo(1);
        } finally {
            deleteCode(VerificationType.UPDATE_EMAIL, email);
        }
    }

    @Test
    void 다른_회원이_쓰는_이메일로는_변경_인증번호를_보내지_않는다() {
        userRepository.save(User.builder()
                .username("owner")
                .email("taken@test.com")
                .snsType(SnsType.MANUAL)
                .build());

        assertThatThrownBy(() ->
                authVerificationService.saveEmailChangeCode(new UserEmailChangeCreateRequest("taken@test.com")))
                .isInstanceOf(DuplicateEmailException.class);
        assertThat(verificationCodeRepository.findByVerificationTypeAndEmail(VerificationType.UPDATE_EMAIL, "taken@test.com")).isEmpty();
    }

    @Test
    void 발송_후_다른_회원이_그_이메일로_가입했다면_변경을_거부한다() {
        saveIssuedCode(VerificationType.UPDATE_EMAIL, "race@test.com");
        userRepository.save(User.builder()
                .username("late")
                .email("race@test.com")
                .snsType(SnsType.MANUAL)
                .build());

        assertThatThrownBy(() ->
                authVerificationService.verifyEmailChange(new UserEmailChangeVerifyRequest("race@test.com", ISSUED_CODE)))
                .isInstanceOf(DuplicateEmailException.class);
    }

    private void saveIssuedCode(VerificationType verificationType, String email) {
        LocalDateTime now = LocalDateTime.now();
        VerificationCode verificationCode = VerificationCode.issue(verificationType, email, ISSUED_CODE, now.plusMinutes(5), now);
        verificationCodeRepository.save(verificationCode);
    }

    private int reloadFailedAttempts(VerificationType verificationType, String email) {
        VerificationCode reloaded = verificationCodeRepository.findByVerificationTypeAndEmail(verificationType, email)
                .orElseThrow();
        return reloaded.getFailedAttempts();
    }

    private void deleteCode(VerificationType verificationType, String email) {
        verificationCodeRepository.findByVerificationTypeAndEmail(verificationType, email)
                .ifPresent(verificationCodeRepository::delete);
    }
}
