package com.mavis.domain.domains.user.domain;

import com.mavis.domain.domains.user.exception.InvalidVerificationCodeException;
import com.mavis.domain.domains.user.exception.VerificationAttemptsExceededException;
import com.mavis.domain.domains.user.exception.VerificationCodeExpiredException;
import com.mavis.domain.domains.user.exception.VerificationResendTooSoonException;
import com.mavis.domain.domains.user.exception.VerificationSendLimitExceededException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VerificationCodeTest {

    private static final LocalDateTime T0 = LocalDateTime.of(2026, 9, 27, 10, 0, 0);
    private static final int CODE = 123456;
    private static final int WRONG = 111111;

    private VerificationCode issuedAt(LocalDateTime now) {
        return VerificationCode.issue(VerificationType.SIGN_UP, "a@test.com", CODE, now.plusMinutes(5), now);
    }

    @Test
    void 첫_발송은_발송횟수_1로_시작한다() {
        VerificationCode verificationCode = issuedAt(T0);

        assertThat(verificationCode.getSendCount()).isEqualTo(1);
        assertThat(verificationCode.getFailedAttempts()).isZero();
        assertThat(verificationCode.getLastSentAt()).isEqualTo(T0);
    }

    @Test
    void 마지막_발송후_1분_안에_재발송하면_거부한다() {
        VerificationCode verificationCode = issuedAt(T0);
        LocalDateTime within = T0.plusSeconds(59);

        assertThatThrownBy(() -> verificationCode.reissue(222222, within.plusMinutes(5), within))
                .isInstanceOf(VerificationResendTooSoonException.class);
    }

    @Test
    void 마지막_발송후_1분이_지나면_재발송할_수_있다() {
        VerificationCode verificationCode = issuedAt(T0);
        LocalDateTime after = T0.plusSeconds(60);

        verificationCode.reissue(222222, after.plusMinutes(5), after);

        assertThat(verificationCode.getSendCount()).isEqualTo(2);
        assertThat(verificationCode.getCode()).isEqualTo(222222);
    }

    @Test
    void 발송창_안에서_10회를_보내면_더_보낼_수_없다() {
        VerificationCode verificationCode = issuedAt(T0);
        LocalDateTime now = T0;
        for (int i = 2; i <= VerificationCode.SEND_LIMIT_PER_WINDOW; i++) {
            now = now.plusMinutes(1);
            verificationCode.reissue(CODE, now.plusMinutes(5), now);
        }
        LocalDateTime next = now.plusMinutes(1);

        assertThat(verificationCode.getSendCount()).isEqualTo(10);
        assertThatThrownBy(() -> verificationCode.reissue(CODE, next.plusMinutes(5), next))
                .isInstanceOf(VerificationSendLimitExceededException.class);
    }

    @Test
    void 발송창_24시간이_지나면_발송횟수가_초기화된다() {
        VerificationCode verificationCode = issuedAt(T0);
        LocalDateTime now = T0;
        for (int i = 2; i <= VerificationCode.SEND_LIMIT_PER_WINDOW; i++) {
            now = now.plusMinutes(1);
            verificationCode.reissue(CODE, now.plusMinutes(5), now);
        }
        LocalDateTime nextDay = T0.plusHours(24);

        verificationCode.reissue(CODE, nextDay.plusMinutes(5), nextDay);

        assertThat(verificationCode.getSendCount()).isEqualTo(1);
        assertThat(verificationCode.getSendWindowStartedAt()).isEqualTo(nextDay);
    }

    @Test
    void 네번_틀리고_다섯번째에_맞히면_통과한다() {
        VerificationCode verificationCode = issuedAt(T0);
        LocalDateTime now = T0.plusMinutes(1);
        for (int i = 0; i < 4; i++) {
            assertThatThrownBy(() -> verificationCode.matchCode(WRONG, now))
                    .isInstanceOf(InvalidVerificationCodeException.class);
        }

        assertThatCode(() -> verificationCode.matchCode(CODE, now)).doesNotThrowAnyException();
        assertThat(verificationCode.getFailedAttempts()).isEqualTo(4);
    }

    @Test
    void 다섯번째로_틀리는_순간_입력횟수_초과를_알린다() {
        VerificationCode verificationCode = issuedAt(T0);
        LocalDateTime now = T0.plusMinutes(1);
        for (int i = 0; i < 4; i++) {
            assertThatThrownBy(() -> verificationCode.matchCode(WRONG, now))
                    .isInstanceOf(InvalidVerificationCodeException.class);
        }

        assertThatThrownBy(() -> verificationCode.matchCode(WRONG, now))
                .isInstanceOf(VerificationAttemptsExceededException.class);
        assertThat(verificationCode.getFailedAttempts()).isEqualTo(5);
    }

    @Test
    void 입력횟수를_초과하면_정답도_거부한다() {
        VerificationCode verificationCode = issuedAt(T0);
        LocalDateTime now = T0.plusMinutes(1);
        for (int i = 0; i < 5; i++) {
            try {
                verificationCode.matchCode(WRONG, now);
            } catch (RuntimeException ignored) {
            }
        }

        assertThatThrownBy(() -> verificationCode.matchCode(CODE, now))
                .isInstanceOf(VerificationAttemptsExceededException.class);
    }

    @Test
    void 재발송하면_틀린횟수와_인증상태가_초기화된다() {
        VerificationCode verificationCode = issuedAt(T0);
        LocalDateTime now = T0.plusMinutes(1);
        for (int i = 0; i < 5; i++) {
            try {
                verificationCode.matchCode(WRONG, now);
            } catch (RuntimeException ignored) {
            }
        }
        LocalDateTime resendAt = T0.plusMinutes(2);

        verificationCode.reissue(654321, resendAt.plusMinutes(5), resendAt);

        assertThat(verificationCode.getFailedAttempts()).isZero();
        assertThat(verificationCode.isVerified()).isFalse();
        assertThatCode(() -> verificationCode.matchCode(654321, resendAt)).doesNotThrowAnyException();
    }

    @Test
    void 만료된_코드는_만료_예외이며_틀린횟수를_올리지_않는다() {
        VerificationCode verificationCode = issuedAt(T0);
        LocalDateTime afterExpiry = T0.plusMinutes(6);

        assertThatThrownBy(() -> verificationCode.matchCode(WRONG, afterExpiry))
                .isInstanceOf(VerificationCodeExpiredException.class);
        assertThat(verificationCode.getFailedAttempts()).isZero();
    }
}
