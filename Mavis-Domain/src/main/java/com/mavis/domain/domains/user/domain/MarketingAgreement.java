package com.mavis.domain.domains.user.domain;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MarketingAgreement {

    private boolean isEmailAgreed;
    private LocalDateTime emailAgreedAt;

    private boolean isSmsAgreed;
    private LocalDateTime smsAgreedAt;

    private MarketingAgreement(boolean isEmailAgreed, LocalDateTime emailAgreedAt, boolean isSmsAgreed, LocalDateTime smsAgreedAt) {
        this.isEmailAgreed = isEmailAgreed;
        this.emailAgreedAt = emailAgreedAt;
        this.isSmsAgreed = isSmsAgreed;
        this.smsAgreedAt = smsAgreedAt;
    }

    public static MarketingAgreement of(boolean isEmailAgreed, boolean isSmsAgreed) {
        LocalDateTime now = LocalDateTime.now();
        return new MarketingAgreement(isEmailAgreed, now, isSmsAgreed, now);
    }

    public void updateEmailAgreement(boolean isEmailAgreed) {
        this.isEmailAgreed = isEmailAgreed;
        this.emailAgreedAt = LocalDateTime.now();
    }

    public void updateSmsAgreement(boolean isSmsAgreed) {
        this.isSmsAgreed = isSmsAgreed;
        this.smsAgreedAt = LocalDateTime.now();
    }
}
