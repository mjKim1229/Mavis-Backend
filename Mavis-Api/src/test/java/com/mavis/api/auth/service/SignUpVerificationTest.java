package com.mavis.api.auth.service;

import com.mavis.api.auth.dto.UserSignUpCodeCreateRequest;
import com.mavis.api.auth.dto.UserSignUpCodeVerifyRequest;
import com.mavis.api.auth.dto.UserSignUpRequest;
import com.mavis.api.support.ControllerTestSupport;
import com.mavis.domain.domains.user.domain.Gender;
import com.mavis.domain.domains.user.domain.SnsType;
import com.mavis.domain.domains.user.domain.User;
import com.mavis.domain.domains.user.domain.VerificationCode;
import com.mavis.domain.domains.user.domain.VerificationType;
import com.mavis.domain.domains.user.exception.DuplicateUsernameException;
import com.mavis.domain.domains.user.exception.EmailNotVerifiedException;
import com.mavis.domain.domains.user.exception.InvalidVerificationCodeException;
import com.mavis.domain.domains.user.repository.UserRepository;
import com.mavis.domain.domains.user.repository.VerificationCodeRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SignUpVerificationTest extends ControllerTestSupport {

    private static final String EMAIL = "signup-test@test.com";

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
}
