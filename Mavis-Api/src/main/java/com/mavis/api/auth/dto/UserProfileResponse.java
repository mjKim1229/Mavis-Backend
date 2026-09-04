package com.mavis.api.auth.dto;

import com.mavis.domain.domains.user.domain.User;

public record UserProfileResponse(
        Long userId,
        String nickname,
        String snsType
) {
    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getNickname(),
                user.getSnsType().getCode()
        );
    }
}
