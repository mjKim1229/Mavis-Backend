package com.mavis.api.auth.dto;

import com.mavis.domain.domains.user.domain.User;

import java.time.LocalDate;

public record UserDetailResponse(
        String username,
        String nickname,
        String name,
        String gender,
        LocalDate birthDay,
        String phoneNumber,
        String email
) {
    public static UserDetailResponse from(User user) {
        return new UserDetailResponse(
                user.getUsername(),
                user.getNickname(),
                user.getName(),
                user.getGender().getTitle(),
                user.getBirthDay(),
                user.getPhoneNumber(),
                user.getEmail()
        );
    }
}
