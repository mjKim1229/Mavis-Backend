package com.mavis.api.auth.dto;

import com.mavis.common.dto.JwtPair;

public record UserOauthResponse(
        Long userId,
        JwtPair jwtPair
) {
}
