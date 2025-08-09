package com.mavis.api.auth.dto;

import com.mavis.common.dto.JwtPair;

public record UserKakaoOauthResponse(
        Long userId,
        JwtPair jwtPair
) {
}
