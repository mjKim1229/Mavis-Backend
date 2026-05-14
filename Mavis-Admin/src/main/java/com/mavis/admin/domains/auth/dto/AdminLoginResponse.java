package com.mavis.admin.domains.auth.dto;

import com.mavis.common.dto.JwtPair;

public record AdminLoginResponse(
        JwtPair jwtPair
) {
}
