package com.mavis.api.auth.dto;

import com.mavis.domain.domains.user.domain.Gender;
import com.mavis.domain.domains.user.domain.MarketingAgreement;
import com.mavis.domain.domains.user.domain.SnsType;
import com.mavis.domain.domains.user.domain.User;

import java.time.LocalDate;

public record UserSignUpRequest(
        String username,
        String password,
        String nickname,
        String name,
        Gender gender,
        LocalDate birthDay,
        String phoneNumber,
        String email,
        boolean isEmailAgreed,
        boolean isSmsAgreed
) {
    public User toEntity(String encodedPassword) {
        return User.builder()
                .username(username)
                .password(encodedPassword)
                .nickname(nickname)
                .name(name)
                .gender(gender)
                .birthDay(birthDay)
                .phoneNumber(phoneNumber)
                .email(email)
                .snsType(SnsType.MANUAL)
                .marketingAgreement(MarketingAgreement.of(isEmailAgreed, isSmsAgreed))
                .build();
    }
}
