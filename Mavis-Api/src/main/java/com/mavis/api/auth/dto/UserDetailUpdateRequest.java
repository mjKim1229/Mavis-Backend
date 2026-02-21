package com.mavis.api.auth.dto;

import com.mavis.domain.domains.user.domain.Gender;

import java.time.LocalDate;

public record UserDetailUpdateRequest(
        String nickname,
        String name,
        Gender gender,
        LocalDate birthDay,
        String phoneNumber
) {
}
