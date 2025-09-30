package com.mavis.api.auth.implement;

import com.mavis.common.dto.JwtPair;
import com.mavis.common.jwt.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserJwtGenerator {
    private final JwtTokenUtil jwtTokenUtil;

    public JwtPair getJwtPair(Long id) {
        String accessToken = jwtTokenUtil.generateAccessToken(id);
        String refreshToken = jwtTokenUtil.generateRefreshToken(id);
        return new JwtPair(accessToken, refreshToken);
    }
}
