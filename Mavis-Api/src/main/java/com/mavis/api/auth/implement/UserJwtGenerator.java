package com.mavis.api.auth.implement;

import com.mavis.common.dto.JwtPair;
import com.mavis.common.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserJwtGenerator {
    private final JwtTokenProvider jwtTokenProvider;

    public JwtPair getJwtPair(Long id) {
        String accessToken = jwtTokenProvider.generateAccessToken(id);
        String refreshToken = jwtTokenProvider.generateRefreshToken(id);
        return new JwtPair(accessToken, refreshToken);
    }
}
