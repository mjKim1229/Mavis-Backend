package com.mavis.infrastructure.outer.api.oauth.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record NaverProfile(
        String id,
        String nickname,
        String name,
        String email,
        String gender,
        String age,
        String birthday,
        String profileImage,
        String birthyear,
        String mobile
) {
}
