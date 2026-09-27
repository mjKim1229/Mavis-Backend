package com.mavis.domain.domains.user.domain;

import com.mavis.domain.domains.common.jpa.BaseEntity;
import com.mavis.domain.domains.user.exception.InvalidVerificationCodeException;
import com.mavis.domain.domains.user.exception.VerificationAttemptsExceededException;
import com.mavis.domain.domains.user.exception.VerificationCodeExpiredException;
import com.mavis.domain.domains.user.exception.VerificationResendTooSoonException;
import com.mavis.domain.domains.user.exception.VerificationSendLimitExceededException;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

@AllArgsConstructor
@Builder
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VerificationCode extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer code;

    @Column(columnDefinition = "varchar(255)")
    @Enumerated(EnumType.STRING)
    private VerificationType verificationType;

    private LocalDateTime expiredAt;

    private String email;

    @Builder.Default
    private boolean isDeleted = false;

    @Comment("인증번호 확인 완료 여부. 회원가입은 이 값이 true인 행이 있어야 진행 가능")
    @Builder.Default
    private boolean isVerified = false;

    @Comment("현재 인증번호로 틀린 횟수. 5회가 되면 인증번호를 다시 받아야 함")
    @Builder.Default
    private int failedAttempts = 0;

    @Comment("발송 횟수를 세기 시작한 뒤 보낸 횟수. 10회가 되면 더 보낼 수 없음")
    @Builder.Default
    private int sendCount = 0;

    @Comment("발송 횟수를 세기 시작한 시각. 이 시각부터 24시간이 지나면 발송 횟수를 0으로 초기화")
    private LocalDateTime sendWindowStartedAt;

    @Comment("마지막 발송 시각. 1분 안에는 다시 보낼 수 없음")
    private LocalDateTime lastSentAt;

    public static final int MAX_FAILED_ATTEMPTS = 5;
    public static final long RESEND_COOLDOWN_SECONDS = 60;
    public static final int SEND_LIMIT_PER_WINDOW = 10;
    public static final long SEND_WINDOW_HOURS = 24;

    public static VerificationCode issue(VerificationType verificationType, String email, Integer code,
                                         LocalDateTime expiredAt, LocalDateTime now) {
        return VerificationCode.builder()
                .verificationType(verificationType)
                .email(email)
                .code(code)
                .expiredAt(expiredAt)
                .sendCount(1)
                .sendWindowStartedAt(now)
                .lastSentAt(now)
                .build();
    }

    public void reissue(Integer code, LocalDateTime expiredAt, LocalDateTime now) {
        LocalDateTime resendAvailableAt = lastSentAt.plusSeconds(RESEND_COOLDOWN_SECONDS);
        if (now.isBefore(resendAvailableAt)) {
            throw VerificationResendTooSoonException.EXCEPTION;
        }
        LocalDateTime windowEndsAt = sendWindowStartedAt.plusHours(SEND_WINDOW_HOURS);
        if (!now.isBefore(windowEndsAt)) {
            this.sendWindowStartedAt = now;
            this.sendCount = 0;
        }
        if (sendCount >= SEND_LIMIT_PER_WINDOW) {
            throw VerificationSendLimitExceededException.EXCEPTION;
        }
        this.code = code;
        this.expiredAt = expiredAt;
        this.isVerified = false;
        this.failedAttempts = 0;
        this.sendCount++;
        this.lastSentAt = now;
    }

    public void matchCode(Integer inputCode, LocalDateTime now) {
        if (isDeleted) {
            throw InvalidVerificationCodeException.EXCEPTION;
        }
        if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
            throw VerificationAttemptsExceededException.EXCEPTION;
        }
        if (expiredAt.isBefore(now)) {
            throw VerificationCodeExpiredException.EXCEPTION;
        }
        if (!code.equals(inputCode)) {
            this.failedAttempts++;
            if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
                throw VerificationAttemptsExceededException.EXCEPTION;
            }
            throw InvalidVerificationCodeException.EXCEPTION;
        }
    }

    public void delete() {
        this.isDeleted = true;
    }

    public void verify(LocalDateTime expiredAt) {
        this.isVerified = true;
        this.expiredAt = expiredAt;
    }

    public boolean isUsableForSignUp(LocalDateTime now) {
        return isVerified && !isDeleted && expiredAt.isAfter(now);
    }
}
