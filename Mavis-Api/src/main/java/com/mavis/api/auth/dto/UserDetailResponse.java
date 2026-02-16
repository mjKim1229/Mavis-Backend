package com.mavis.api.auth.dto;

import com.mavis.domain.domains.user.domain.Gender;
import com.mavis.domain.domains.user.domain.User;

import java.time.LocalDate;

public record UserDetailResponse(
        Long userId,
        String nickname,
        String name,
        Gender gender,
        LocalDate birthDay,
        String phoneNumber,
        String email
) {
    public static UserDetailResponse from(User user) {
        return new UserDetailResponse(
                user.getId(),
                user.getNickname(),
                user.getName(),
                Gender.valueOf(user.getGender()),
                user.getBirthDay(),
                user.getPhoneNumber(),
                user.getEmail()
        );
    }
}
